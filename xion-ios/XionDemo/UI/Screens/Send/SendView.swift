import SwiftUI

struct SendView: View {
    @ObservedObject var viewModel: SendViewModel
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 20) {
                    if let txHash = viewModel.txHash {
                        // Success state
                        VStack(spacing: 16) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 64))
                                .foregroundStyle(.green)

                            Text("Transaction Sent")
                                .font(.xionHeadlineMedium)

                            AddressDisplay(address: txHash)

                            Button("Done") { dismiss() }
                                .buttonStyle(.borderedProminent)
                                .tint(.xionGreen)
                        }
                        .padding(.top, 40)
                    } else {
                        // Form
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Recipient Address")
                                .font(.xionLabelMedium)
                            TextField("xion1...", text: $viewModel.recipient)
                                .textFieldStyle(.roundedBorder)
                                .autocapitalization(.none)
                                .onChange(of: viewModel.recipient) { _ in viewModel.validateRecipient() }
                            if let err = viewModel.recipientError {
                                Text(err).font(.xionLabelSmall).foregroundStyle(.red)
                            }
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Amount (\(Constants.displayDenom))")
                                .font(.xionLabelMedium)
                            TextField("0.0", text: $viewModel.amount)
                                .textFieldStyle(.roundedBorder)
                                .keyboardType(.decimalPad)
                                .onChange(of: viewModel.amount) { _ in viewModel.validateAmount() }
                            if let err = viewModel.amountError {
                                Text(err).font(.xionLabelSmall).foregroundStyle(.red)
                            }
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Memo (optional)")
                                .font(.xionLabelMedium)
                            TextField("", text: $viewModel.memo)
                                .textFieldStyle(.roundedBorder)
                        }

                        Button(action: viewModel.confirmSend) {
                            Text("Send")
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.xionGreen)
                        .disabled(!viewModel.isFormValid)
                        .clipShape(RoundedRectangle(cornerRadius: 12))

                        ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    }
                }
                .padding(24)
            }

            LoadingOverlay(isVisible: viewModel.isLoading, message: "Sending transaction...")
        }
        .navigationTitle("Send")
        .navigationBarTitleDisplayMode(.inline)
        .confirmationDialog("Confirm Transaction", isPresented: $viewModel.showConfirmation) {
            Button("Send \(viewModel.amount) \(Constants.displayDenom)", action: viewModel.send)
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Send \(viewModel.amount) \(Constants.displayDenom) to \(viewModel.recipient)?")
        }
    }
}
