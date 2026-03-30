import SwiftUI

struct OfframpView: View {
    @StateObject var viewModel: OfframpViewModel
    let onDone: () -> Void

    var body: some View {
        ZStack {
            Color.screenBackground.ignoresSafeArea()

            switch viewModel.step {
            case .form:
                OfframpFormContent(viewModel: viewModel)
            case .depositing:
                OfframpDepositingContent()
            case .processing:
                OfframpProcessingContent()
            case .status:
                OfframpStatusContent(viewModel: viewModel, onDone: onDone)
            }

            // Error overlay at top
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                Spacer()
            }
        }
        .navigationTitle("Cash Out")
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

private struct OfframpFormContent: View {
    @ObservedObject var viewModel: OfframpViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Convert stablecoins to USD and withdraw to your bank account")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.subtitleText)

                Spacer().frame(height: 24)

                // Bank status card
                HStack(spacing: 12) {
                    Image(systemName: viewModel.bankLinked ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(viewModel.bankLinked ? Color.xionGreen : Color.xionOrange)

                    Text(viewModel.bankLinked ? (viewModel.bankName ?? "Bank Account Linked") : "Link a bank account first (use Buy flow)")
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(Color.greetingText)

                    Spacer()
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                Spacer().frame(height: 20)

                // Amount input
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

                // Info card
                VStack(spacing: 8) {
                    OfframpDetailRow(
                        label: "You send",
                        value: viewModel.amount.isEmpty ? "\u{2014}" : "~\(viewModel.amount) SBC"
                    )
                    OfframpDetailRow(
                        label: "You receive",
                        value: viewModel.amount.isEmpty ? "$0.00" : "$\(viewModel.amount)"
                    )
                    OfframpDetailRow(label: "Method", value: "Same-Day ACH", valueColor: Color.subtitleText)
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                if viewModel.custodialAddress != nil {
                    Spacer().frame(height: 12)
                    Text("Stablecoins will be sent to Brale custodial address for processing")
                        .font(.system(size: 12))
                        .foregroundStyle(Color.subtitleText)
                }

                Spacer().frame(height: 24)

                // Cash Out button
                Button(action: { viewModel.submitOfframp() }) {
                    Text("Cash Out")
                        .font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(viewModel.isFormValid && !viewModel.isLoading ? Color.mintscanBlue : Color.mintscanBlue.opacity(0.5))
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

// MARK: - Depositing

private struct OfframpDepositingContent: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            ProgressView()
                .scaleEffect(1.5)
                .tint(.mintscanBlue)

            Spacer().frame(height: 8)

            Text("Sending stablecoins to Brale...")
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(Color.greetingText)

            Text("Broadcasting on-chain transaction")
                .font(.system(size: 13))
                .foregroundStyle(Color.subtitleText)

            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(48)
    }
}

// MARK: - Processing

private struct OfframpProcessingContent: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            ProgressView()
                .scaleEffect(1.5)
                .tint(.mintscanBlue)

            Spacer().frame(height: 8)

            Text("Creating cash out transfer...")
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(Color.greetingText)

            Text("Setting up ACH credit to your bank")
                .font(.system(size: 13))
                .foregroundStyle(Color.subtitleText)

            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(48)
    }
}

// MARK: - Status

private struct OfframpStatusContent: View {
    @ObservedObject var viewModel: OfframpViewModel
    let onDone: () -> Void

    private var hasError: Bool {
        viewModel.error != nil && !viewModel.depositConfirmed
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 32)

                Image(systemName: hasError ? "xmark.circle.fill" : "checkmark.circle.fill")
                    .font(.system(size: 64))
                    .foregroundStyle(hasError ? Color.xionRed : Color.xionGreen)

                Spacer().frame(height: 12)

                Text(hasError ? "Cash Out Failed" : "Cash Out Submitted!")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(Color.greetingText)

                if !hasError {
                    Spacer().frame(height: 8)
                    Text("Your stablecoins have been sent to Brale. The USD will be deposited to your bank account within 1\u{2013}3 business days via ACH.")
                        .font(.system(size: 13))
                        .foregroundStyle(Color.subtitleText)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 16)
                }

                Spacer().frame(height: 20)

                // Transfer details card
                VStack(spacing: 8) {
                    if let transfer = viewModel.transfer {
                        OfframpDetailRow(label: "Transfer ID", value: String(transfer.id.prefix(12)) + "...")
                        OfframpDetailRow(label: "Amount", value: "$\(transfer.amount.value) \(transfer.amount.currency)")
                    }
                    if viewModel.depositConfirmed {
                        OfframpDetailRow(label: "On-chain Deposit", value: "Confirmed", valueColor: Color.xionGreen)
                    }
                    OfframpDetailRow(label: "Bank Transfer", value: "In Progress")
                    if let txHash = viewModel.depositTxHash {
                        OfframpDetailRow(label: "Tx Hash", value: String(txHash.prefix(16)) + "...")
                    }
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                Spacer().frame(height: 24)

                Button(action: {
                    viewModel.reset()
                    onDone()
                }) {
                    Text("Done")
                        .font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.mintscanBlue)
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

private struct OfframpDetailRow: View {
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
