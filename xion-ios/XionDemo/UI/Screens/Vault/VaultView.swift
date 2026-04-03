import SwiftUI

struct VaultView: View {
    @ObservedObject var viewModel: VaultViewModel
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Vault balance card
                HStack {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Vault Balance")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.subtitleText)

                        if viewModel.isBalanceLoading {
                            ProgressView().tint(.xionOrange)
                        } else {
                            Text(viewModel.vaultBalance.map { CoinFormatter.formatWithDenom($0) } ?? "0 XION")
                                .font(.system(size: 28, weight: .bold))
                                .foregroundStyle(Color.greetingText)
                        }
                    }
                    Spacer()
                    Image(systemName: "lock.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(Color.subtitleText)
                }
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                if let txHash = viewModel.txHash {
                    // Success state
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Transaction Successful")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(Color.xionGreen)
                        Text(txHash)
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(Color.subtitleText)
                            .lineLimit(1)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(Color.xionGreen.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    Button(action: viewModel.resetState) {
                        Text("New Transaction")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.xionOrange)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                } else {
                    // Amount input
                    TextField("Amount (XION)", text: $viewModel.amount)
                        .keyboardType(.decimalPad)
                        .textFieldStyle(.roundedBorder)
                        .disabled(viewModel.isLoading)

                    // Deposit / Withdraw
                    HStack(spacing: 12) {
                        Button(action: viewModel.deposit) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Deposit")
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.xionGreen.opacity(viewModel.amount.isEmpty || viewModel.isLoading ? 0.5 : 1.0))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(viewModel.amount.isEmpty || viewModel.isLoading)

                        Button(action: viewModel.withdraw) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Withdraw")
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.mintscanBlue.opacity(viewModel.amount.isEmpty || viewModel.isLoading ? 0.5 : 1.0))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(viewModel.amount.isEmpty || viewModel.isLoading)
                    }

                    // Withdraw All
                    Button(action: viewModel.withdrawAll) {
                        Text("Withdraw All")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color.subtitleText.opacity(0.3), lineWidth: 1)
                            )
                            .foregroundStyle(Color.greetingText)
                    }
                    .buttonStyle(.plain)
                    .disabled(viewModel.isLoading || viewModel.vaultBalance == nil || viewModel.vaultBalance == "0")
                }

                // Error
                ErrorBanner(
                    message: viewModel.error,
                    onDismiss: viewModel.clearError,
                    onRetry: nil
                )
            }
            .padding(24)
        }
        .background(Color.screenBackground)
        .navigationTitle("Vault")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { viewModel.loadVaultBalance() }
    }
}
