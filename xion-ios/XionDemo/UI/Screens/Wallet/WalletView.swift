import SwiftUI

struct WalletView: View {
    @ObservedObject var viewModel: WalletViewModel
    let onNavigateToContract: () -> Void
    let onNavigateToHistory: () -> Void
    let onNavigateToOnramp: () -> Void
    let onNavigateToOfframp: () -> Void
    let onNavigateToLinkBank: () -> Void
    let onDisconnected: () -> Void

    @State private var showSendSheet = false
    @StateObject private var sendViewModel: SendViewModel

    init(
        viewModel: WalletViewModel,
        sendViewModel: SendViewModel,
        onNavigateToContract: @escaping () -> Void,
        onNavigateToHistory: @escaping () -> Void,
        onNavigateToOnramp: @escaping () -> Void,
        onNavigateToOfframp: @escaping () -> Void,
        onNavigateToLinkBank: @escaping () -> Void,
        onDisconnected: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self._sendViewModel = StateObject(wrappedValue: sendViewModel)
        self.onNavigateToContract = onNavigateToContract
        self.onNavigateToHistory = onNavigateToHistory
        self.onNavigateToOnramp = onNavigateToOnramp
        self.onNavigateToOfframp = onNavigateToOfframp
        self.onNavigateToLinkBank = onNavigateToLinkBank
        self.onDisconnected = onDisconnected
    }

    var body: some View {
        List {
            // Use a single Section with no header to get clean list appearance
            Section {
                // Header row: Disconnect | Address + Copy
                if let address = viewModel.address {
                    let shortAddress = "\(address.prefix(8))...\(address.suffix(4))"
                    HStack {
                        Button(action: viewModel.disconnect) {
                            HStack(spacing: 4) {
                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                    .font(.system(size: 14))
                                Text("Disconnect")
                                    .font(.system(size: 13))
                            }
                            .foregroundStyle(.red)
                        }
                        .buttonStyle(.plain)

                        Spacer()

                        HStack(spacing: 0) {
                            Text(shortAddress)
                                .font(.system(size: 13, design: .monospaced))
                                .foregroundStyle(Color.subtitleText)

                            Button(action: {
                                UIPasteboard.general.string = address
                            }) {
                                Image(systemName: "doc.on.doc")
                                    .font(.system(size: 14))
                                    .foregroundStyle(Color.subtitleText)
                            }
                            .buttonStyle(.plain)
                            .frame(width: 32, height: 32)
                        }
                    }
                    .listRowBackground(Color.screenBackground)
                    .listRowSeparator(.hidden)
                }

                // Grant status warnings
                if !viewModel.grantsActive {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .foregroundStyle(.red)
                            .font(.system(size: 14))
                        Text("Session grants expired. Please reconnect.")
                            .font(.system(size: 11, weight: .medium))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.red.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .listRowBackground(Color.screenBackground)
                    .listRowSeparator(.hidden)
                }

                if viewModel.sessionExpiryWarning {
                    HStack(spacing: 8) {
                        Image(systemName: "timer")
                            .foregroundStyle(.orange)
                            .font(.system(size: 14))
                        Text("Session expiring soon. Please reconnect.")
                            .font(.system(size: 11, weight: .medium))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.orange.opacity(0.1))
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .listRowBackground(Color.screenBackground)
                    .listRowSeparator(.hidden)
                }

                // Balance card
                VStack(alignment: .leading, spacing: 8) {
                    Text("XION Balance")
                        .font(.system(size: 14))
                        .foregroundStyle(Color.subtitleText)

                    if viewModel.isBalanceLoading {
                        ProgressView()
                            .tint(.xionOrange)
                    } else {
                        Text(viewModel.balance.map { CoinFormatter.formatWithDenom($0) } ?? "\u{2014}")
                            .font(.system(size: 28, weight: .bold))
                            .foregroundStyle(Color.greetingText)
                            .lineLimit(1)
                    }

                    if viewModel.sbcBalance != nil {
                        Divider()
                            .padding(.vertical, 8)

                        Text("Stablecoin Balance")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.subtitleText)

                        Text(viewModel.sbcBalance.map { CoinFormatter.formatWithDenom($0, denom: Constants.sbcDisplayDenom) } ?? "\u{2014}")
                            .font(.system(size: 22, weight: .bold))
                            .foregroundStyle(Color.greetingText)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)
                .listRowBackground(Color.screenBackground)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 10, leading: 24, bottom: 0, trailing: 24))

                // Send Tokens button
                Button(action: { showSendSheet = true }) {
                    HStack(spacing: 8) {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 16))
                        Text("Send Tokens")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.xionOrange)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .buttonStyle(.plain)
                .listRowBackground(Color.screenBackground)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 24, leading: 24, bottom: 0, trailing: 24))

                // Bank link status
                if !viewModel.bankLinked {
                    HStack(spacing: 12) {
                        Image(systemName: "building.columns.fill")
                            .font(.system(size: 24))
                            .foregroundStyle(Color.subtitleText)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Link Bank Account")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundStyle(Color.greetingText)
                            Text("Required for buying and selling stablecoins")
                                .font(.system(size: 12))
                                .foregroundStyle(Color.subtitleText)
                        }
                        Spacer()
                        Button(action: onNavigateToLinkBank) {
                            Text("Link")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundStyle(.white)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(Color.xionOrange)
                                .clipShape(RoundedRectangle(cornerRadius: 8))
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(16)
                    .background(Color.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: Color.cardShadow, radius: 2, y: 1)
                } else {
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(Color.xionGreen)
                        Text("Bank Linked")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.subtitleText)
                    }
                }

                // Buy and Cash Out buttons side-by-side
                HStack(spacing: 12) {
                    Button(action: onNavigateToOnramp) {
                        HStack(spacing: 6) {
                            Image(systemName: "plus.circle.fill")
                                .font(.system(size: 16))
                            Text("Buy")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.xionGreen.opacity(viewModel.bankLinked ? 1.0 : 0.5))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                    .disabled(!viewModel.bankLinked)

                    Button(action: onNavigateToOfframp) {
                        HStack(spacing: 6) {
                            Image(systemName: "banknote.fill")
                                .font(.system(size: 16))
                            Text("Cash Out")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.mintscanBlue.opacity(viewModel.bankLinked ? 1.0 : 0.5))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                    .buttonStyle(.plain)
                    .disabled(!viewModel.bankLinked)
                }
                .listRowBackground(Color.screenBackground)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 0, trailing: 24))

                // Recent Transactions header
                Text("Recent Transactions")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(Color.greetingText)
                    .listRowBackground(Color.screenBackground)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets(top: 24, leading: 24, bottom: 0, trailing: 24))

                // Transaction card
                VStack(spacing: 0) {
                    if viewModel.transactions.isEmpty {
                        Text("No transactions yet")
                            .font(.system(size: 14))
                            .foregroundStyle(Color.subtitleText)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 48)
                    } else {
                        let recentTxs = Array(viewModel.transactions.prefix(3))
                        ForEach(Array(recentTxs.enumerated()), id: \.element.id) { index, tx in
                            CompactTransactionRow(transaction: tx)
                            if index < recentTxs.count - 1 {
                                Divider()
                                    .padding(.horizontal, 16)
                            }
                        }
                    }
                }
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)
                .listRowBackground(Color.screenBackground)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 0, trailing: 24))

                // View on Mintscan link
                if let address = viewModel.address {
                    Button(action: {
                        if let url = URL(string: "https://www.mintscan.io/xion-testnet/address/\(address)") {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        Text("View all on Mintscan \u{2192}")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundStyle(Color.mintscanBlue)
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.plain)
                    .listRowBackground(Color.screenBackground)
                    .listRowSeparator(.hidden)
                    .listRowInsets(EdgeInsets(top: 12, leading: 24, bottom: 0, trailing: 24))
                }

                // Error banner
                ErrorBanner(
                    message: viewModel.error,
                    onDismiss: viewModel.clearError,
                    onRetry: { viewModel.refresh() }
                )
                .listRowBackground(Color.screenBackground)
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 32, leading: 24, bottom: 24, trailing: 24))
            }
        }
        .listStyle(.plain)
        .background(Color.screenBackground)
        .scrollContentBackground(.hidden)
        .refreshable {
            viewModel.refresh()
        }
        .onAppear {
            viewModel.refresh()
        }
        .onChange(of: viewModel.isDisconnected) { disconnected in
            if disconnected { onDisconnected() }
        }
        .sheet(isPresented: $showSendSheet) {
            sendViewModel.resetState()
            viewModel.refresh()
        } content: {
            SendSheetContent(
                viewModel: sendViewModel,
                onDone: {
                    showSendSheet = false
                }
            )
            .presentationDetents([.fraction(0.55), .large])
            .presentationDragIndicator(.visible)
        }
    }
}
