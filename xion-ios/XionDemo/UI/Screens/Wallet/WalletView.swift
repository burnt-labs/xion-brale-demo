import SwiftUI

struct WalletView: View {
    @ObservedObject var viewModel: WalletViewModel
    let onNavigateToSend: () -> Void
    let onNavigateToContract: () -> Void
    let onNavigateToHistory: () -> Void
    let onDisconnected: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                // Greeting
                let shortAddress: String = {
                    guard let addr = viewModel.address, addr.count > 12 else { return "" }
                    return "\(addr.prefix(8))...\(addr.suffix(4))"
                }()

                Text("Hi \(shortAddress),")
                    .font(.system(size: 20))
                    .foregroundStyle(Color.subtitleText)
                    .padding(.top, 16)

                Text("What would you like to do?")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundStyle(Color.greetingText)
                    .padding(.top, 4)

                // Grant status warnings
                if !viewModel.grantsActive {
                    WarningBanner(
                        icon: "exclamationmark.triangle.fill",
                        message: "Session grants expired. Please reconnect.",
                        color: .red
                    )
                    .padding(.top, 12)
                }

                if viewModel.sessionExpiryWarning {
                    WarningBanner(
                        icon: "timer",
                        message: "Session expiring soon. Please reconnect.",
                        color: .orange
                    )
                    .padding(.top, 8)
                }

                // Balance card
                VStack(spacing: 4) {
                    if viewModel.isBalanceLoading {
                        ProgressView()
                            .tint(.xionOrange)
                    } else {
                        Text(viewModel.balance.map { CoinFormatter.formatWithDenom($0) } ?? "—")
                            .font(.system(size: 32, weight: .bold))
                            .foregroundStyle(Color.greetingText)
                    }
                    Text("Available Balance")
                        .font(.system(size: 14))
                        .foregroundStyle(Color.subtitleText)
                }
                .frame(maxWidth: .infinity)
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)
                .padding(.top, 24)

                // 2x2 Action cards
                LazyVGrid(columns: [
                    GridItem(.flexible(), spacing: 12),
                    GridItem(.flexible(), spacing: 12)
                ], spacing: 12) {
                    ActionCardView(
                        title: "Send",
                        description: "Transfer tokens to another address",
                        systemImage: "arrow.up.right",
                        iconColor: Color(red: 0, green: 0.48, blue: 1),
                        action: onNavigateToSend
                    )
                    ActionCardView(
                        title: "Receive",
                        description: "Show your wallet address",
                        systemImage: "qrcode",
                        iconColor: Color(red: 0.2, green: 0.78, blue: 0.35),
                        action: { /* TODO */ }
                    )
                    ActionCardView(
                        title: "Contract",
                        description: "Execute smart contracts",
                        systemImage: "chevron.left.forwardslash.chevron.right",
                        iconColor: Color(red: 1, green: 0.58, blue: 0),
                        action: onNavigateToContract
                    )
                    ActionCardView(
                        title: "History",
                        description: "View past transactions",
                        systemImage: "clock.arrow.circlepath",
                        iconColor: Color(red: 0.69, green: 0.32, blue: 0.87),
                        action: onNavigateToHistory
                    )
                }
                .padding(.top, 24)

                // Recent Transactions
                if !viewModel.transactions.isEmpty {
                    HStack {
                        Text("Recent Transactions")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(Color.greetingText)
                        Spacer()
                        Button("See All", action: onNavigateToHistory)
                            .font(.system(size: 14, weight: .medium))
                            .foregroundStyle(Color.xionOrange)
                    }
                    .padding(.top, 24)

                    VStack(spacing: 8) {
                        ForEach(viewModel.transactions) { tx in
                            TransactionRow(transaction: tx)
                        }
                    }
                    .padding(.top, 4)
                }

                // Disconnect
                Button(role: .destructive, action: viewModel.disconnect) {
                    Label("Disconnect", systemImage: "rectangle.portrait.and.arrow.right")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderless)
                .foregroundStyle(.red)
                .padding(.top, 32)

                // Error
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.top, 8)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
        .background(Color.screenBackground)
        .onChange(of: viewModel.isDisconnected) { disconnected in
            if disconnected { onDisconnected() }
        }
    }
}

private struct WarningBanner: View {
    let icon: String
    let message: String
    let color: Color

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .foregroundStyle(color)
                .font(.caption)
            Text(message)
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(.primary)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(color.opacity(0.1))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
