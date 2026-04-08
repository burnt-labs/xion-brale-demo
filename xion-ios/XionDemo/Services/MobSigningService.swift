import Foundation
import Mob


protocol MobSigningServiceProtocol {
    func createClientWithSigner(mnemonic: String) async throws -> String
    func upgradeToSessionClient(metaAccountAddress: String, treasuryAddress: String, sessionExpiresAt: Int64) async throws
    func getBalance(address: String, denom: String) async throws -> BalanceInfo
    func getHeight() async throws -> Int64
    func send(toAddress: String, coins: [Coin], memo: String?) async throws -> TransactionResult
    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Coin],
        memo: String?,
        gasLimit: UInt64?
    ) async throws -> TransactionResult
    func queryContractSmart(contractAddress: String, queryMsg: Data) async throws -> Data
    func getTx(txHash: String) async throws -> TransactionResult
    func getSignerAddress() -> String?
    func disconnect()
}

final class MobSigningService: MobSigningServiceProtocol {

    private let queue = DispatchQueue(label: "com.burnt.xiondemo.mob", qos: .userInitiated)

    private var client: Client?
    private var signer: RustSigner?

    func createClientWithSigner(mnemonic: String) async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
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
                        gasPrice: Constants.gasPrice,
                        feeGranter: nil
                    )

                    let newSigner = try RustSigner.fromMnemonic(
                        mnemonic: mnemonic,
                        addressPrefix: Constants.addressPrefix,
                        derivationPath: Constants.derivationPath
                    )

                    // Client init queries the account on-chain, which fails for new
                    // session keys that don't yet have an account. Save the signer
                    // regardless — the session client created in upgradeToSessionClient
                    // will handle session signing via the meta-account.
                    self.signer = newSigner
                    do {
                        let newClient = try Client.newWithSigner(config: config, signer: newSigner)
                        self.client = newClient
                    } catch {
                        // Account may not exist yet — that's OK for session key flow
                        self.client = nil
                    }

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

    func upgradeToSessionClient(metaAccountAddress: String, treasuryAddress: String, sessionExpiresAt: Int64) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            queue.async {
                do {
                    guard let currentSigner = self.signer else {
                        throw MobServiceError.signerNotInitialized
                    }

                    let now = UInt64(Date().timeIntervalSince1970)
                    let metadata = SessionMetadata(
                        granter: metaAccountAddress,
                        grantee: currentSigner.address(),
                        feeGranter: treasuryAddress,
                        feePayer: nil,
                        createdAt: now,
                        expiresAt: UInt64(sessionExpiresAt),
                        description: nil
                    )

                    let config = ChainConfig(
                        chainId: Constants.chainId,
                        rpcEndpoint: Constants.rpcUrl,
                        grpcEndpoint: nil,
                        addressPrefix: Constants.addressPrefix,
                        coinType: Constants.coinType,
                        gasPrice: Constants.gasPrice,
                        feeGranter: treasuryAddress
                    )

                    let oldClient = self.client
                    self.client = nil

                    let sessionClient = try Client.newWithSessionSigner(
                        config: config,
                        signer: currentSigner,
                        metadata: metadata
                    )
                    self.client = sessionClient

                    // Deferred cleanup
                    if oldClient != nil {
                        DispatchQueue.global().asyncAfter(deadline: .now() + 2) {
                            _ = oldClient
                        }
                    }

                    continuation.resume()
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

    func send(toAddress: String, coins: [Coin], memo: String?) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let response = try client.send(toAddress: toAddress, amount: coins, memo: memo)
                    continuation.resume(returning: TransactionResult(
                        txHash: response.txhash,
                        success: response.code == 0,
                        gasUsed: "\(response.gasUsed)",
                        gasWanted: "\(response.gasWanted)",
                        height: response.height,
                        rawLog: response.rawLog
                    ))
                } catch {
                    print("[MobSigningService] send error: \(error)")
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Coin],
        memo: String?,
        gasLimit: UInt64? = nil
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
                        granter: nil,
                        feeGranter: nil,
                        memo: memo,
                        gasLimit: gasLimit
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
                    print("[MobSigningService] executeContract error: \(error)")
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func queryContractSmart(contractAddress: String, queryMsg: Data) async throws -> Data {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobServiceError.clientNotInitialized
                    }
                    let result = try client.queryContractSmart(contractAddress: contractAddress, queryMsg: queryMsg)
                    continuation.resume(returning: result)
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
