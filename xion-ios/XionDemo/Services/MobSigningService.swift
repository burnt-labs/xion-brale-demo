import Foundation


protocol MobSigningServiceProtocol {
    func createClientWithSigner(mnemonic: String) async throws -> String
    func getBalance(address: String, denom: String) async throws -> BalanceInfo
    func getHeight() async throws -> Int64
    func send(toAddress: String, coins: [Coin], granter: String?, feeGranter: String?, memo: String?) async throws -> TransactionResult
    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Coin],
        granter: String?,
        feeGranter: String?,
        memo: String?
    ) async throws -> TransactionResult
    func getTx(txHash: String) async throws -> TransactionResult
    func getSignerAddress() -> String?
    func disconnect()
}

final class MobSigningService: MobSigningServiceProtocol {

    private let queue = DispatchQueue(label: "com.burnt.xiondemo.mob", qos: .userInitiated)

    private var client: Client?
    private var signer: Signer?

    func createClientWithSigner(mnemonic: String) async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    // Defer cleanup of old resources to avoid racing with in-flight RPCs
                    let oldClient = self.client
                    let oldSigner = self.signer
                    self.client = nil
                    self.signer = nil

                    let config = ChainConfig(
                        chainId: Constants.chainId,
                        rpcEndpoint: Constants.rpcUrl,
                        grpcEndpoint: nil,
                        addressPrefix: Constants.addressPrefix,
                        coinType: Constants.coinType,
                        gasPrice: Constants.gasPrice
                    )

                    let newSigner = try Signer.fromMnemonic(
                        mnemonic: mnemonic,
                        addressPrefix: Constants.addressPrefix,
                        derivationPath: Constants.derivationPath
                    )

                    // Retry once on transient cold-start failure
                    let newClient: Client
                    do {
                        newClient = try Client.newWithSigner(config: config, signer: newSigner)
                    } catch {
                        Thread.sleep(forTimeInterval: 0.5)
                        newClient = try Client.newWithSigner(config: config, signer: newSigner)
                    }

                    self.signer = newSigner
                    self.client = newClient

                    // Deferred cleanup so in-flight RPCs on old client can finish
                    if oldClient != nil || oldSigner != nil {
                        DispatchQueue.global().asyncAfter(deadline: .now() + 2) {
                            _ = oldClient
                            _ = oldSigner
                        }
                    }

                    continuation.resume(returning: newSigner.address())
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func getBalance(address: String, denom: String) async throws -> BalanceInfo {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let coin = try client.getBalance(address: address, denom: denom)
                    continuation.resume(returning: BalanceInfo(amount: coin.amount, denom: coin.denom))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func getHeight() async throws -> Int64 {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let height = try client.getHeight()
                    continuation.resume(returning: Int64(clamping: height))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func send(toAddress: String, coins: [Coin], granter: String?, feeGranter: String?, memo: String?) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let response = try client.send(toAddress: toAddress, amount: coins, granter: granter, feeGranter: feeGranter, memo: memo)
                    continuation.resume(returning: TransactionResult(
                        txHash: response.txhash,
                        success: response.code == 0,
                        gasUsed: "\(response.gasUsed)",
                        gasWanted: "\(response.gasWanted)",
                        height: response.height,
                        rawLog: response.rawLog
                    ))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Coin],
        granter: String?,
        feeGranter: String?,
        memo: String?
    ) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let response = try client.executeContract(
                        contractAddress: contractAddress,
                        msg: msg,
                        funds: funds,
                        granter: granter,
                        feeGranter: feeGranter,
                        memo: memo
                    )
                    continuation.resume(returning: TransactionResult(
                        txHash: response.txhash,
                        success: response.code == 0,
                        gasUsed: "\(response.gasUsed)",
                        gasWanted: "\(response.gasWanted)",
                        height: response.height,
                        rawLog: response.rawLog
                    ))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func getTx(txHash: String) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let response = try client.getTx(hash: txHash)
                    continuation.resume(returning: TransactionResult(
                        txHash: response.txhash,
                        success: response.code == 0,
                        gasUsed: "\(response.gasUsed)",
                        gasWanted: "\(response.gasWanted)",
                        height: response.height,
                        rawLog: response.rawLog
                    ))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func getSignerAddress() -> String? {
        signer?.address()
    }

    func disconnect() {
        let oldClient = client
        let oldSigner = signer
        client = nil
        signer = nil
        // Deferred cleanup so in-flight RPCs can finish
        if oldClient != nil || oldSigner != nil {
            DispatchQueue.global().asyncAfter(deadline: .now() + 2) {
                _ = oldClient
                _ = oldSigner
            }
        }
    }
}

enum MobServiceError: LocalizedError {
    case libraryNotAvailable
    case signerNotInitialized
    case clientNotInitialized

    var errorDescription: String? {
        switch self {
        case .libraryNotAvailable:
            return "Real mob library not available. Build with scripts/build-mob-ios.sh"
        case .signerNotInitialized:
            return "Signer not initialized"
        case .clientNotInitialized:
            return "Client not initialized"
        }
    }
}
