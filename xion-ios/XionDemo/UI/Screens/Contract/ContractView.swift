import SwiftUI

struct ContractView: View {
    @ObservedObject var viewModel: ContractViewModel
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

                            Text("Contract Executed")
                                .font(.xionHeadlineMedium)

                            AddressDisplay(address: txHash)

                            Button("Execute Another", action: viewModel.reset)
                                .buttonStyle(.borderedProminent)
                                .tint(.xionGreen)

                            Button("Done") { dismiss() }
                                .buttonStyle(.bordered)
                        }
                        .padding(.top, 40)
                    } else {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Contract Address")
                                .font(.xionLabelMedium)
                            TextField("xion1...", text: $viewModel.contractAddress)
                                .textFieldStyle(.roundedBorder)
                                .autocapitalization(.none)
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Message (JSON)")
                                .font(.xionLabelMedium)
                            TextEditor(text: $viewModel.message)
                                .font(.system(.body, design: .monospaced))
                                .frame(minHeight: 120)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(.secondary.opacity(0.3), lineWidth: 1)
                                )
                        }

                        VStack(alignment: .leading, spacing: 4) {
                            Text("Funds (uxion, optional)")
                                .font(.xionLabelMedium)
                            TextField("0", text: $viewModel.funds)
                                .textFieldStyle(.roundedBorder)
                                .keyboardType(.numberPad)
                        }

                        Button(action: viewModel.execute) {
                            Text("Execute")
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

            LoadingOverlay(isVisible: viewModel.isLoading, message: "Executing contract...")
        }
        .navigationTitle("Execute Contract")
        .navigationBarTitleDisplayMode(.inline)
    }
}
