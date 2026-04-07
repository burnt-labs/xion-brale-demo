import SwiftUI

struct SendSheetContent: View {
    @ObservedObject var viewModel: SendViewModel
    let onDone: () -> Void

    @State private var showConfirm = false

    var body: some View {
        ZStack {
            if viewModel.txResult != nil {
                SuccessContent(viewModel: viewModel, onDone: onDone)
            } else if viewModel.isLoading {
                LoadingContent()
            } else if showConfirm {
                ConfirmContent(
                    viewModel: viewModel,
                    onCancel: { showConfirm = false },
                    onConfirm: {
                        showConfirm = false
                        viewModel.send()
                    }
                )
            } else {
                FormContent(
                    viewModel: viewModel,
                    onReview: { showConfirm = true }
                )
            }

            // Error overlay at top
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                Spacer()
            }
        }
    }
}

// MARK: - Form

private struct FormContent: View {
    @ObservedObject var viewModel: SendViewModel

    let onReview: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Send Tokens")
                    .font(.system(size: 22, weight: .bold))
                    .padding(.bottom, 20)

                // Token selector
                Picker("Token", selection: $viewModel.selectedToken) {
                    ForEach(SendToken.allCases, id: \.self) { token in
                        Text(token.rawValue).tag(token)
                    }
                }
                .pickerStyle(.segmented)
                .padding(.bottom, 16)

                // Recipient
                SendTextField(
                    label: "Recipient Address",
                    placeholder: "xion1...",
                    text: Binding(
                        get: { viewModel.recipient },
                        set: { viewModel.updateRecipient($0) }
                    ),
                    error: viewModel.recipientError,
                    trailing: {
                        Button(action: {
                            if let clip = UIPasteboard.general.string {
                                viewModel.updateRecipient(clip)
                            }
                        }) {
                            Image(systemName: "doc.on.clipboard")
                                .font(.system(size: 16))
                                .foregroundStyle(.secondary)
                        }
                    }
                )

                Spacer().frame(height: 16)

                // Amount
                SendTextField(
                    label: "Amount (\(viewModel.selectedToken.rawValue))",
                    placeholder: "0.0",
                    text: Binding(
                        get: { viewModel.amount },
                        set: { viewModel.updateAmount($0) }
                    ),
                    error: viewModel.amountError,
                    keyboardType: .decimalPad
                )

                Spacer().frame(height: 16)

                // Memo
                SendTextField(
                    label: "Memo (optional)",
                    placeholder: "",
                    text: Binding(
                        get: { viewModel.memo },
                        set: { viewModel.updateMemo($0) }
                    )
                )

                Spacer().frame(height: 24)

                // Fee estimate
                HStack {
                    Text("Estimated Fee")
                        .font(.system(size: 14))
                    Spacer()
                    Text("~0.00625 XION")
                        .font(.system(size: 14))
                        .foregroundStyle(.secondary)
                }
                .padding(16)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Spacer().frame(height: 24)

                // Review button
                Button(action: onReview) {
                    Text("Review Transaction")
                        .font(.system(size: 16, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
                .buttonStyle(.borderedProminent)
                .tint(.accentColor)
                .disabled(!viewModel.isFormValid)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Spacer().frame(height: 32)
            }
            .padding(24)
        }
    }
}

// MARK: - Confirm

private struct ConfirmContent: View {
    @ObservedObject var viewModel: SendViewModel
    let onCancel: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Confirm Transaction")
                .font(.system(size: 22, weight: .bold))
                .padding(.bottom, 16)

            ConfirmRow(label: "To", value: viewModel.recipient)
            ConfirmRow(label: "Token", value: viewModel.selectedToken.rawValue)
            ConfirmRow(label: "Amount", value: "\(viewModel.amount) \(viewModel.selectedToken.rawValue)")
            if !viewModel.memo.isEmpty {
                ConfirmRow(label: "Memo", value: viewModel.memo)
            }
            ConfirmRow(label: "Est. Fee", value: "~0.00625 XION")

            Spacer().frame(height: 24)

            HStack(spacing: 12) {
                Button(action: onCancel) {
                    Text("Cancel")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                }
                .buttonStyle(.bordered)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Button(action: onConfirm) {
                    Text("Confirm")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                }
                .buttonStyle(.borderedProminent)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            Spacer().frame(height: 32)
        }
        .padding(24)
    }
}

// MARK: - Loading

private struct LoadingContent: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            ProgressView()
                .scaleEffect(1.5)
            Text("Broadcasting transaction...")
                .font(.system(size: 16))
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .padding(48)
    }
}

// MARK: - Success

private struct SuccessContent: View {
    @ObservedObject var viewModel: SendViewModel
    let onDone: () -> Void

    private var txResult: TransactionResult? { viewModel.txResult }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 64))
                    .foregroundStyle(.green)

                Spacer().frame(height: 12)

                Text("Transaction Sent!")
                    .font(.system(size: 22, weight: .bold))

                Spacer().frame(height: 20)

                // Details card
                if let tx = txResult {
                    VStack(alignment: .leading, spacing: 0) {
                        // Tx Hash
                        Text("Tx Hash")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(.secondary)

                        Spacer().frame(height: 4)

                        HStack {
                            Text(tx.txHash)
                                .font(.system(size: 13, design: .monospaced))
                                .lineLimit(1)
                                .truncationMode(.middle)

                            Spacer()

                            Button(action: {
                                UIPasteboard.general.string = tx.txHash
                            }) {
                                Image(systemName: "doc.on.doc")
                                    .font(.system(size: 14))
                                    .foregroundStyle(.secondary)
                            }
                            .frame(width: 32, height: 32)
                        }

                        Divider()
                            .padding(.vertical, 12)

                        DetailRow(label: "Status", value: tx.success ? "Success" : "Failed",
                                  valueColor: tx.success ? .green : .red)
                        if tx.height > 0 {
                            DetailRow(label: "Block Height", value: formatNumber(tx.height))
                        }
                        if let gasUsed = Int64(tx.gasUsed), gasUsed > 0 {
                            DetailRow(label: "Gas Used", value: formatNumber(gasUsed))
                        }
                        if let gasWanted = Int64(tx.gasWanted), gasWanted > 0 {
                            DetailRow(label: "Gas Wanted", value: formatNumber(gasWanted))
                        }
                    }
                    .padding(16)
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer().frame(height: 24)

                Button(action: onDone) {
                    Text("Done")
                        .font(.system(size: 16, weight: .medium))
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                }
                .buttonStyle(.borderedProminent)
                .clipShape(RoundedRectangle(cornerRadius: 12))

                Spacer().frame(height: 32)
            }
            .padding(24)
        }
    }

    private func formatNumber(_ value: Int64) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.locale = Locale(identifier: "en_US")
        return formatter.string(from: NSNumber(value: value)) ?? "\(value)"
    }
}

// MARK: - Helpers

private struct DetailRow: View {
    let label: String
    let value: String
    var valueColor: Color = .primary

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 14))
                .foregroundStyle(.secondary)
            Spacer()
            Text(value)
                .font(.system(size: 14, weight: .medium))
                .foregroundStyle(valueColor)
        }
        .padding(.vertical, 6)
    }
}

private struct ConfirmRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 14))
                .foregroundStyle(.secondary)
            Spacer()
            Text(value)
                .font(.system(size: 14))
        }
        .padding(.vertical, 8)
    }
}

private struct SendTextField<Trailing: View>: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var error: String? = nil
    var keyboardType: UIKeyboardType = .default
    var trailing: (() -> Trailing)?

    init(
        label: String,
        placeholder: String,
        text: Binding<String>,
        error: String? = nil,
        keyboardType: UIKeyboardType = .default,
        @ViewBuilder trailing: @escaping () -> Trailing
    ) {
        self.label = label
        self.placeholder = placeholder
        self._text = text
        self.error = error
        self.keyboardType = keyboardType
        self.trailing = trailing
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                TextField(placeholder, text: $text)
                    .keyboardType(keyboardType)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()

                if let trailing = trailing {
                    trailing()
                }
            }
            .padding(14)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(error != nil ? Color.red : Color(.systemGray4), lineWidth: 1)
            )

            // Floating label above field
            Text(label)
                .font(.system(size: 12))
                .foregroundStyle(error != nil ? .red : .secondary)
                .padding(.horizontal, 4)
                .background(Color(.systemBackground))
                .offset(x: 10, y: -8)
                .padding(.top, -8)

            if let error = error {
                Text(error)
                    .font(.system(size: 11))
                    .foregroundStyle(.red)
                    .padding(.leading, 14)
                    .padding(.top, 2)
            }
        }
    }
}

extension SendTextField where Trailing == EmptyView {
    init(
        label: String,
        placeholder: String,
        text: Binding<String>,
        error: String? = nil,
        keyboardType: UIKeyboardType = .default
    ) {
        self.label = label
        self.placeholder = placeholder
        self._text = text
        self.error = error
        self.keyboardType = keyboardType
        self.trailing = nil
    }
}
