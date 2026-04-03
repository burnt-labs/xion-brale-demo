import Foundation


final class XionRepositoryImpl: XionRepositoryProtocol {

    let sessionManager: SessionManager
    private let mobService: MobSigningServiceProtocol

    init(sessionManager: SessionManager, mobService: MobSigningServiceProtocol) {
        self.sessionManager = sessionManager
        self.mobService = mobService
    }

    func connect() async throws -> String {
        try await sessionManager.authenticate()
    }

    func restoreSession() async -> Bool {
        await sessionManager.restoreSession()
    }

    func getBalance() async throws -> BalanceInfo {
        guard let address = sessionManager.walletState.metaAccountAddress else {
            throw RepositoryError.notConnected
        }
        return try await mobService.getBalance(address: address, denom: Constants.coinDenom)
    }

    func getSbcBalance() async throws -> BalanceInfo {
        guard let address = sessionManager.walletState.metaAccountAddress else {
            throw RepositoryError.notConnected
        }
        return try await mobService.getBalance(address: address, denom: Constants.braleSbcOnChainDenom)
    }

    func getBlockHeight() async throws -> Int64 {
        try await mobService.getHeight()
    }

    func send(toAddress: String, amount: String, memo: String, denom: String = Constants.coinDenom) async throws -> TransactionResult {
        let state = sessionManager.walletState
        guard state.isConnected else { throw RepositoryError.notConnected }

        let coins = [Coin(denom: denom, amount: amount)]
        let result = try await withGrantRecovery {
            try await self.mobService.send(
                toAddress: toAddress,
                coins: coins,
                memo: memo.isEmpty ? nil : memo
            )
        }
        let confirmed = result.success ? (await awaitTxConfirmation(txHash: result.txHash) ?? result) : result
        sessionManager.appendTransaction(confirmed)
        return confirmed
    }

    func executeContract(contractAddress: String, msg: String, funds: String?) async throws -> TransactionResult {
        let state = sessionManager.walletState
        guard state.isConnected else { throw RepositoryError.notConnected }

        let fundsCoins: [Coin] = funds.map { [Coin(denom: Constants.coinDenom, amount: $0)] } ?? []
        let msgData = Data(msg.utf8)
        let result = try await withGrantRecovery {
            try await self.mobService.executeContract(
                contractAddress: contractAddress,
                msg: msgData,
                funds: fundsCoins,
                memo: nil
            )
        }
        let confirmed = result.success ? (await awaitTxConfirmation(txHash: result.txHash) ?? result) : result
        sessionManager.appendTransaction(confirmed)
        return confirmed
    }

    func getTx(txHash: String) async throws -> TransactionResult {
        try await mobService.getTx(txHash: txHash)
    }

    func getRecentTransactions(address: String) async throws -> [TransactionResult] {
        async let sent = fetchTxsByQuery(address: address, queryKey: "transfer.sender")
        async let received = fetchTxsByQuery(address: address, queryKey: "transfer.recipient")

        let all = try await sent + received
        return all
            .reduce(into: [String: TransactionResult]()) { dict, tx in
                if dict[tx.txHash] == nil { dict[tx.txHash] = tx }
            }
            .values
            .sorted { $0.height > $1.height }
            .prefix(3)
            .map { $0 }
    }

    @MainActor
    func appendTransaction(_ tx: TransactionResult) {
        sessionManager.appendTransaction(tx)
    }

    @MainActor
    func disconnect() {
        sessionManager.disconnect()
    }

    // MARK: - Transaction Confirmation Polling

    private func awaitTxConfirmation(txHash: String, maxAttempts: Int = 10) async -> TransactionResult? {
        for _ in 0..<maxAttempts {
            try? await Task.sleep(nanoseconds: 1_500_000_000)
            if let result = try? await mobService.getTx(txHash: txHash) {
                return result
            }
        }
        return nil
    }

    // MARK: - REST Transaction History

    private func fetchTxsByQuery(address: String, queryKey: String) async throws -> [TransactionResult] {
        let encodedQuery = "\(queryKey)%3D%27\(address)%27"
        let urlString = "\(Constants.restUrl)cosmos/tx/v1beta1/txs?query=\(encodedQuery)&order_by=ORDER_BY_DESC&pagination.limit=3"
        guard let url = URL(string: urlString) else { return [] }

        let (data, response) = try await URLSession.shared.data(from: url)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else { return [] }

        guard let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
              let txResponses = json["tx_responses"] as? [[String: Any]] else { return [] }

        let txs = json["txs"] as? [[String: Any]]

        return txResponses.enumerated().compactMap { index, txResponse in
            let txBody = txs?[safe: index]

            let txHash = txResponse["txhash"] as? String ?? ""
            let code = txResponse["code"] as? Int ?? -1
            let gasUsed = txResponse["gas_used"] as? String ?? "0"
            let gasWanted = txResponse["gas_wanted"] as? String ?? "0"
            let height = txResponse["height"] as? String ?? "0"
            let rawLog = txResponse["raw_log"] as? String ?? ""
            let timestamp = txResponse["timestamp"] as? String ?? ""

            // Extract fee
            let feeAmount = (txBody?["auth_info"] as? [String: Any])
                .flatMap { $0["fee"] as? [String: Any] }
                .flatMap { $0["amount"] as? [[String: Any]] }
                .flatMap { $0.first }
                .flatMap { $0["amount"] as? String } ?? "0"

            // Extract tx type from messages
            let messages = (txBody?["body"] as? [String: Any])?["messages"] as? [[String: Any]]
            let txType: String = {
                guard let msgs = messages, let first = msgs.first,
                      let typeUrl = first["@type"] as? String else { return "" }
                let shortType = typeUrl.components(separatedBy: ".").last ?? typeUrl
                return msgs.count > 1 ? "\(shortType) +\(msgs.count - 1)" : shortType
            }()

            // Extract amount and recipient from transfer events
            var transferAmount = ""
            var transferRecipient = ""
            if let events = txResponse["events"] as? [[String: Any]] {
                for event in events {
                    guard (event["type"] as? String) == "transfer",
                          let attrs = event["attributes"] as? [[String: Any]] else { continue }
                    var attrMap = [String: String]()
                    for attr in attrs {
                        if let key = attr["key"] as? String, let value = attr["value"] as? String {
                            attrMap[key] = value
                        }
                    }
                    let sender = attrMap["sender"] ?? ""
                    let recipient = attrMap["recipient"] ?? ""
                    if sender == address || recipient == address {
                        transferAmount = (attrMap["amount"] ?? "").replacingOccurrences(of: "[^0-9]", with: "", options: .regularExpression)
                        transferRecipient = recipient
                        break
                    }
                }
            }

            return TransactionResult(
                txHash: txHash,
                success: code == 0,
                gasUsed: gasUsed,
                gasWanted: gasWanted,
                height: Int64(height) ?? 0,
                rawLog: rawLog,
                timestamp: timestamp,
                fee: feeAmount,
                txType: txType,
                amount: transferAmount,
                recipient: transferRecipient
            )
        }
    }

    // MARK: - Grant Recovery

    private func withGrantRecovery<T>(_ block: () async throws -> T) async throws -> T {
        do {
            return try await block()
        } catch {
            let message = error.localizedDescription.lowercased()
            if message.contains("authorization not found") || message.contains("fee allowance not found") {
                await sessionManager.setGrantsActive(false)
                throw GrantExpiredError()
            }
            throw error
        }
    }
}

struct GrantExpiredError: LocalizedError {
    var errorDescription: String? { "Session grants have expired. Please reconnect." }
}

enum RepositoryError: LocalizedError {
    case notConnected

    var errorDescription: String? {
        switch self {
        case .notConnected: return "Wallet not connected"
        }
    }
}

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
