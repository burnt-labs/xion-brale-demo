import SwiftUI

struct ConnectView: View {
    @ObservedObject var viewModel: ConnectViewModel
    let onConnected: () -> Void

    var body: some View {
        ZStack {
            Color.screenBackground
                .ignoresSafeArea()

            VStack(alignment: .leading, spacing: 0) {
                Spacer()

                Text("Welcome,")
                    .font(.system(size: 32))
                    .foregroundStyle(Color.greetingText)

                Text("Connect to get started")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundStyle(Color.greetingText)
                    .padding(.top, 4)

                Spacer()

                // Connect card
                VStack(alignment: .leading, spacing: 16) {
                    Image(systemName: "person.circle.fill")
                        .font(.system(size: 36))
                        .foregroundStyle(Color.xionOrange)

                    Text("Connect with XION")
                        .font(.system(size: 18, weight: .semibold))

                    Text("Sign in with your Meta Account for a walletless blockchain experience. No keys to manage.")
                        .font(.system(size: 14))
                        .foregroundStyle(Color.subtitleText)
                        .lineSpacing(4)

                    Button(action: viewModel.startOAuthFlow) {
                        Text("Continue")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.xionOrange)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .padding(24)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .shadow(color: Color.cardShadow, radius: 4, y: 2)

                Spacer().frame(height: 32)
            }
            .padding(24)

            // Error banner
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError, onRetry: viewModel.retryRestore)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                Spacer()
            }

            // Loading overlay
            LoadingOverlay(isVisible: viewModel.isLoading, message: viewModel.loadingMessage)
        }
        .onChange(of: viewModel.isConnected) { connected in
            if connected { onConnected() }
        }
    }
}
