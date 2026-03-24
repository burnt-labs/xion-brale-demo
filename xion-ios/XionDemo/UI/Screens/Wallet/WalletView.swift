import SwiftUI

struct WalletView: View {
    @ObservedObject var viewModel: WalletViewModel
    let onNavigateToContract: () -> Void
    let onNavigateToHistory: () -> Void
    let onDisconnected: () -> Void

    @State private var showSendSheet = false
    @StateObject private var sendViewModel: SendViewModel

    init(
        viewModel: WalletViewModel,
        sendViewModel: SendViewModel,
        onNavigateToContract: @escaping () -> Void,
        onNavigateToHistory: @escaping () -> Void,
        onDisconnected: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self._sendViewModel = StateObject(wrappedValue: sendViewModel)
        self.onNavigateToContract = onNavigateToContract
        self.onNavigateToHistory = onNavigateToHistory
        self.onDisconnected = onDisconnected
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Spacer().frame(height: 48)

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
                            .frame(width: 32, height: 32)
                        }
                    }
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
                    .padding(.top, 12)
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
                    .padding(.top, 8)
                }

                // Balance card
                VStack(alignment: .leading, spacing: 8) {
                    Text("Available Balance")
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
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)
                .padding(.top, 20)

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
                .padding(.top, 24)

                // Recent Transactions header
                Text("Recent Transactions")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(Color.greetingText)
                    .padding(.top, 24)

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
                .padding(.top, 12)

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
                    .padding(.top, 12)
                }

                // Error banner
                ErrorBanner(
                    message: viewModel.error,
                    onDismiss: viewModel.clearError,
                    onRetry: { viewModel.refresh() }
                )
                .padding(.top, 32)

                Spacer().frame(height: 16)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
        .background(Color.screenBackground)
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
