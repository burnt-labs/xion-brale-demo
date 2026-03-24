import Foundation

protocol XionRepositoryProtocol {
    var sessionManager: SessionManager { get }

    func connect() async throws -> String
    func restoreSession() async -> Bool
    func getBalance() async throws -> BalanceInfo
    func getBlockHeight() async throws -> Int64
    func send(toAddress: String, amount: String, memo: String) async throws -> TransactionResult
    func executeContract(contractAddress: String, msg: String, funds: String?) async throws -> TransactionResult
    func getTx(txHash: String) async throws -> TransactionResult
    func getRecentTransactions(address: String) async throws -> [TransactionResult]
    @MainActor func disconnect()
}
