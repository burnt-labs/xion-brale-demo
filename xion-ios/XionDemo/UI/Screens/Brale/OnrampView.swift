import SwiftUI

struct OnrampView: View {
    @StateObject var viewModel: OnrampViewModel
    let onDone: () -> Void

    var body: some View {
        ZStack {
            Color.screenBackground.ignoresSafeArea()

            switch viewModel.step {
            case .form:
                OnrampFormContent(viewModel: viewModel)
            case .processing:
                OnrampProcessingContent()
            case .status:
                OnrampStatusContent(viewModel: viewModel, onDone: onDone)
            }

            // Error overlay at top
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                Spacer()
            }
        }
        .navigationTitle("Buy Stablecoins")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: onDone) {
                    Image(systemName: "chevron.left")
                        .foregroundStyle(Color.greetingText)
                }
            }
        }
    }
}

// MARK: - Form

private struct OnrampFormContent: View {
    @ObservedObject var viewModel: OnrampViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Convert USD from your bank account to stablecoins on Xion")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.subtitleText)

                Spacer().frame(height: 24)

                // Bank account status card
                HStack(spacing: 12) {
                    Image(systemName: viewModel.bankLinked ? "checkmark.circle.fill" : "building.columns.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(viewModel.bankLinked ? Color.xionGreen : Color.subtitleText)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(viewModel.bankLinked ? (viewModel.bankName ?? "Bank Account Linked") : "No Bank Account")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundStyle(Color.greetingText)

                        if !viewModel.bankLinked {
                            Text("Link your bank account via Plaid to buy stablecoins")
                                .font(.system(size: 12))
                                .foregroundStyle(Color.subtitleText)
                        }
                    }

                    Spacer()

                    if !viewModel.bankLinked {
                        Button(action: { viewModel.requestPlaidLinkToken(name: "User", email: "user@example.com") }) {
                            if viewModel.isLoading {
                                ProgressView()
                                    .tint(.white)
                                    .frame(width: 40, height: 32)
                            } else {
                                Text("Link")
                                    .font(.system(size: 13, weight: .semibold))
                                    .foregroundStyle(.white)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(Color.xionOrange)
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                            }
                        }
                        .disabled(viewModel.isLoading)
                    }
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                Spacer().frame(height: 20)

                // Amount input (USD)
                VStack(alignment: .leading, spacing: 4) {
                    Text("Amount (USD)")
                        .font(.system(size: 12))
                        .foregroundStyle(Color.subtitleText)

                    HStack {
                        Text("$")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundStyle(Color.greetingText)

                        TextField("100.00", text: Binding(
                            get: { viewModel.amount },
                            set: { viewModel.updateAmount($0) }
                        ))
                        .keyboardType(.decimalPad)
                        .font(.system(size: 18))
                    }
                    .padding(14)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(viewModel.amountError != nil ? Color.red : Color(.systemGray4), lineWidth: 1)
                    )

                    if let error = viewModel.amountError {
                        Text(error)
                            .font(.system(size: 11))
                            .foregroundStyle(.red)
                            .padding(.leading, 4)
                    }
                }

                Spacer().frame(height: 16)

                // Fee info card
                VStack(spacing: 8) {
                    OnrampDetailRow(
                        label: "You pay",
                        value: viewModel.amount.isEmpty ? "$0.00" : "$\(viewModel.amount)"
                    )
                    OnrampDetailRow(
                        label: "You receive",
                        value: viewModel.amount.isEmpty ? "\u{2014}" : "~\(viewModel.amount) SBC"
                    )
                    OnrampDetailRow(label: "Method", value: "ACH Debit", valueColor: Color.subtitleText)
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                Spacer().frame(height: 24)

                // Buy Stablecoins button
                Button(action: { viewModel.submitOnramp() }) {
                    Text("Buy Stablecoins")
                        .font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(viewModel.isFormValid && !viewModel.isLoading ? Color.xionOrange : Color.xionOrange.opacity(0.5))
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
                .disabled(!viewModel.isFormValid || viewModel.isLoading)

                Spacer().frame(height: 32)
            }
            .padding(24)
        }
    }
}

// MARK: - Processing

private struct OnrampProcessingContent: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            ProgressView()
                .scaleEffect(1.5)
                .tint(.xionOrange)

            Spacer().frame(height: 8)

            Text("Waiting for tokens...")
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(Color.greetingText)

            Text("Checking your wallet for incoming stablecoins")
                .font(.system(size: 13))
                .foregroundStyle(Color.subtitleText)

            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(48)
    }
}

// MARK: - Status

private struct OnrampStatusContent: View {
    @ObservedObject var viewModel: OnrampViewModel
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 32)

                Image(systemName: viewModel.tokensReceived ? "checkmark.circle.fill" : "clock.fill")
                    .font(.system(size: 64))
                    .foregroundStyle(viewModel.tokensReceived ? Color.xionGreen : Color.xionOrange)

                Spacer().frame(height: 12)

                Text(viewModel.tokensReceived ? "Tokens Received!" : "Transfer Submitted")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(Color.greetingText)

                if viewModel.tokensReceived, let received = viewModel.receivedAmount {
                    Spacer().frame(height: 8)
                    Text("\(CoinFormatter.formatWithDenom(received, denom: Constants.sbcDisplayDenom)) added to your wallet")
                        .font(.system(size: 14))
                        .foregroundStyle(Color.subtitleText)
                }

                if !viewModel.tokensReceived {
                    Spacer().frame(height: 8)
                    Text("Tokens are being minted. This may take a few minutes.")
                        .font(.system(size: 13))
                        .foregroundStyle(Color.subtitleText)
                        .multilineTextAlignment(.center)
                }

                Spacer().frame(height: 20)

                // Transfer details card
                if let transfer = viewModel.transfer {
                    VStack(spacing: 8) {
                        OnrampDetailRow(label: "Transfer ID", value: String(transfer.id.prefix(12)) + "...")
                        OnrampDetailRow(label: "Amount", value: "$\(transfer.amount.value) \(transfer.amount.currency)")
                        OnrampDetailRow(
                            label: "Tokens",
                            value: viewModel.tokensReceived ? "Received" : "Pending",
                            valueColor: viewModel.tokensReceived ? Color.xionGreen : Color.xionOrange
                        )
                        if let createdAt = transfer.createdAt {
                            OnrampDetailRow(label: "Created", value: String(createdAt.prefix(19)).replacingOccurrences(of: "T", with: " "))
                        }
                    }
                    .padding(16)
                    .background(Color.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: Color.cardShadow, radius: 2, y: 1)
                }

                Spacer().frame(height: 24)

                Button(action: {
                    viewModel.reset()
                    onDone()
                }) {
                    Text(viewModel.tokensReceived ? "Done" : "Close")
                        .font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.xionOrange)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer().frame(height: 32)
            }
            .padding(24)
        }
    }
}

// MARK: - Helpers

private struct OnrampDetailRow: View {
    let label: String
    let value: String
    var valueColor: Color = Color.greetingText

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 13))
                .foregroundStyle(Color.subtitleText)
            Spacer()
            Text(value)
                .font(.system(size: 13, weight: .medium))
                .foregroundStyle(valueColor)
        }
    }
}
