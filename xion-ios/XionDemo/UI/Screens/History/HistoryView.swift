import SwiftUI

struct HistoryView: View {
    @ObservedObject var viewModel: HistoryViewModel

    var body: some View {
        Group {
            if viewModel.isLoading && viewModel.transactions.isEmpty {
                ProgressView("Loading transactions...")
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.transactions.isEmpty {
                VStack(spacing: 16) {
                    Spacer()
                    Image(systemName: "clock.badge.questionmark")
                        .font(.system(size: 48))
                        .foregroundStyle(.secondary)
                    Text("No transactions yet")
                        .font(.xionHeadlineMedium)
                        .foregroundStyle(.secondary)
                    Text("Transactions will appear here after you send tokens or execute contracts.")
                        .font(.xionBodySmall)
                        .foregroundStyle(.tertiary)
                        .multilineTextAlignment(.center)
                    Spacer()
                }
                .padding(24)
            } else {
                List(viewModel.transactions) { tx in
                    Button {
                        viewModel.selectedTransaction = tx
                    } label: {
                        TransactionRow(transaction: tx)
                    }
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("History")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $viewModel.selectedTransaction) { tx in
            TransactionDetailSheet(transaction: tx)
        }
    }
}

private struct TransactionDetailSheet: View {
    let transaction: TransactionResult
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List {
                Section("Transaction Hash") {
                    AddressDisplay(address: transaction.txHash)
                }
                Section("Details") {
                    LabeledContent("Status", value: transaction.success ? "Success" : "Failed")
                    LabeledContent("Height", value: "\(transaction.height)")
                    LabeledContent("Gas Used", value: transaction.gasUsed)
                    LabeledContent("Gas Wanted", value: transaction.gasWanted)
                    if !transaction.fee.isEmpty && transaction.fee != "0" {
                        LabeledContent("Fee", value: "\(transaction.fee) \(Constants.coinDenom)")
                    }
                    if !transaction.txType.isEmpty {
                        LabeledContent("Type", value: transaction.txType)
                    }
                    if !transaction.timestamp.isEmpty {
                        LabeledContent("Time", value: transaction.timestamp)
                    }
                }
                if !transaction.amount.isEmpty {
                    Section("Transfer") {
                        LabeledContent("Amount", value: CoinFormatter.formatWithDenom(transaction.amount))
                        if !transaction.recipient.isEmpty {
                            LabeledContent("Recipient") {
                                AddressDisplay(address: transaction.recipient)
                            }
                        }
                    }
                }
                if !transaction.rawLog.isEmpty {
                    Section("Raw Log") {
                        Text(transaction.rawLog)
                            .font(.system(.caption, design: .monospaced))
                    }
                }
            }
            .navigationTitle("Transaction")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
    }
}
