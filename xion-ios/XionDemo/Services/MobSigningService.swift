import Foundation
import Mob

protocol MobSigningServiceProtocol {
    func createClientWithSigner(mnemonic: String) async throws -> String
    func getBalance(address: String, denom: String) async throws -> BalanceInfo
    func getHeight() async throws -> Int64
    func send(toAddress: String, coins: [Mob.Coin], memo: String?) async throws -> TransactionResult
    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Mob.Coin],
        memo: String?
    ) async throws -> TransactionResult
    func getTx(txHash: String) async throws -> TransactionResult
    func getSignerAddress() -> String?
    func disconnect()
}

final class MobSigningService: MobSigningServiceProtocol {

    private let queue = DispatchQueue(label: "com.burnt.xiondemo.mob", qos: .userInitiated)

    private var client: Mob.Client?
    private var signer: Mob.Signer?

    func createClientWithSigner(mnemonic: String) async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    let config = Mob.ChainConfig(
                        chainId: Constants.chainId,
                        rpcEndpoint: Constants.rpcUrl,
                        grpcEndpoint: nil,
                        addressPrefix: Constants.addressPrefix,
                        coinType: 118,
                        gasPrice: Constants.gasPrice
                    )

                    let newSigner = try Mob.Signer.fromMnemonic(
                        mnemonic: mnemonic,
                        addressPrefix: Constants.addressPrefix,
                        derivationPath: "m/44'/118'/0'/0/0"
                    )
                    self.signer = newSigner

                    let newClient = try Mob.Client.newWithSigner(
                        config: config,
                        signer: newSigner
                    )
                    self.client = newClient

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
                        throw MobError.clientNotInitialized
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
                        throw MobError.clientNotInitialized
                    }
                    let height = try client.getHeight()
                    continuation.resume(returning: Int64(height))
                } catch {
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func send(toAddress: String, coins: [Mob.Coin], memo: String?) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobError.clientNotInitialized
                    }
                    // Note: mob's send() sends FROM the session key address directly.
                    // For authz-delegated sends, mob would need granter/feeGranter support.
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
                    continuation.resume(throwing: error)
                }
            }
        }
    }

    func executeContract(
        contractAddress: String,
        msg: Data,
        funds: [Mob.Coin],
        memo: String?
    ) async throws -> TransactionResult {
        try await withCheckedThrowingContinuation { continuation in
            queue.async {
                do {
                    guard let client = self.client else {
                        throw MobError.clientNotInitialized
                    }
                    let response = try client.executeContract(
                        contractAddress: contractAddress,
                        msg: msg,
                        funds: funds,
                        granter: nil,
                        feeGranter: nil,
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
                        throw MobError.clientNotInitialized
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
        signer = nil
        client = nil
    }
}

enum MobError: LocalizedError {
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
