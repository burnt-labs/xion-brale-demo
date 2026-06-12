import Foundation

struct TransactionResult: Identifiable, Codable {
    var id: String { txHash }

    let txHash: String
    let success: Bool
    let gasUsed: String
    let gasWanted: String
    let height: Int64
    let rawLog: String
    var timestamp: String = ""
    var fee: String = ""
    var txType: String = ""
    var amount: String = ""
    var amountDenom: String = ""
    var recipient: String = ""
    // The other party of an on-chain transfer (sender if received, recipient if sent),
    // used to tell a peer SBC transfer apart from a Brale on/offramp leg.
    var counterparty: String = ""
    // Brale-sourced rows: human status (Pending/Processing/Completed/...), a
    // preformatted amount (e.g. "$1.00") shown instead of the micro-unit `amount`,
    // and whether the transfer is still settling.
    var status: String = ""
    var displayAmount: String = ""
    var inProgress: Bool = false
}
