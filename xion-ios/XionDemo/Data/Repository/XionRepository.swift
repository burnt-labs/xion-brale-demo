import Foundation

protocol XionRepositoryProtocol {
    var sessionManager: SessionManager { get }

    func connect() async throws -> String
    func restoreSession() async -> Bool
    func getBalance() async throws -> BalanceInfo
    func getSbcBalance() async throws -> BalanceInfo
    func getBlockHeight() async throws -> Int64
    func send(toAddress: String, amount: String, memo: String, denom: String) async throws -> TransactionResult
    func executeContract(contractAddress: String, msg: String, funds: String?) async throws -> TransactionResult
    func getVaultBalance() async throws -> BalanceInfo
    func vaultDeposit(amount: String, denom: String) async throws -> TransactionResult
    func vaultWithdraw(amount: String, denom: String) async throws -> TransactionResult
    func vaultWithdrawAll() async throws -> TransactionResult
    func getTx(txHash: String) async throws -> TransactionResult
    func getRecentTransactions(address: String) async throws -> [TransactionResult]
    @MainActor func appendTransaction(_ tx: TransactionResult)
    @MainActor func disconnect()
}

extension XionRepositoryProtocol {
    func send(toAddress: String, amount: String, memo: String) async throws -> TransactionResult {
        try await send(toAddress: toAddress, amount: amount, memo: memo, denom: Constants.coinDenom)
    }
    func vaultDeposit(amount: String) async throws -> TransactionResult {
        try await vaultDeposit(amount: amount, denom: Constants.coinDenom)
    }
    func vaultWithdraw(amount: String) async throws -> TransactionResult {
        try await vaultWithdraw(amount: amount, denom: Constants.coinDenom)
    }
}
