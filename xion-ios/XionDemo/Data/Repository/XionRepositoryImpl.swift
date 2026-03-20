import Foundation
import Mob

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

    func getBlockHeight() async throws -> Int64 {
        try await mobService.getHeight()
    }

    func send(toAddress: String, amount: String, memo: String) async throws -> TransactionResult {
        // Note: mob's send() sends FROM the session key address directly.
        // For authz-delegated sends, mob would need granter/feeGranter support.
        let coins = [Mob.Coin(denom: Constants.coinDenom, amount: amount)]
        let result = try await withGrantRecovery {
            try await self.mobService.send(
                toAddress: toAddress,
                coins: coins,
                memo: memo.isEmpty ? nil : memo
            )
        }
        sessionManager.appendTransaction(result)
        return result
    }

    func executeContract(contractAddress: String, msg: String, funds: String?) async throws -> TransactionResult {
        let fundsCoins: [Mob.Coin] = funds.map { [Mob.Coin(denom: Constants.coinDenom, amount: $0)] } ?? []
        let msgData = Data(msg.utf8)
        let result = try await withGrantRecovery {
            try await self.mobService.executeContract(
                contractAddress: contractAddress,
                msg: msgData,
                funds: fundsCoins,
                memo: nil
            )
        }
        sessionManager.appendTransaction(result)
        return result
    }

    func getTx(txHash: String) async throws -> TransactionResult {
        try await mobService.getTx(txHash: txHash)
    }

    func disconnect() {
        sessionManager.disconnect()
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
