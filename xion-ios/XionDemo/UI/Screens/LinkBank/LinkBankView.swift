import SwiftUI

struct LinkBankView: View {
    @StateObject var viewModel: LinkBankViewModel
    let onDone: () -> Void

    var body: some View {
        ZStack {
            Color.screenBackground.ignoresSafeArea()

            if viewModel.bankLinked {
                LinkedContent(viewModel: viewModel, onDone: onDone)
            } else {
                LinkFormContent(viewModel: viewModel)
            }

            // Error overlay at top
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                Spacer()
            }
        }
        .navigationTitle("Link Bank Account")
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

// MARK: - Link Form

private struct LinkFormContent: View {
    @ObservedObject var viewModel: LinkBankViewModel

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text("Connect your bank account via Plaid to enable stablecoin purchases")
                    .font(.system(size: 14))
                    .foregroundStyle(Color.subtitleText)

                Spacer().frame(height: 24)

                VStack(alignment: .leading, spacing: 12) {
                    LinkBankTextField(
                        label: "Legal Name",
                        placeholder: "John Doe",
                        text: Binding(get: { viewModel.userName }, set: { viewModel.updateUserName($0) }),
                        error: viewModel.userNameError,
                        icon: "person.fill"
                    )

                    LinkBankTextField(
                        label: "Email Address",
                        placeholder: "you@example.com",
                        text: Binding(get: { viewModel.userEmail }, set: { viewModel.updateUserEmail($0) }),
                        error: viewModel.userEmailError,
                        icon: "envelope.fill",
                        keyboardType: .emailAddress
                    )

                    LinkBankTextField(
                        label: "Phone Number",
                        placeholder: "+15551234567",
                        text: Binding(get: { viewModel.userPhone }, set: { viewModel.updateUserPhone($0) }),
                        error: viewModel.userPhoneError,
                        icon: "phone.fill",
                        keyboardType: .phonePad
                    )

                    LinkBankTextField(
                        label: "Date of Birth",
                        placeholder: "1990-01-15",
                        text: Binding(get: { viewModel.userDob }, set: { viewModel.updateUserDob($0) }),
                        error: viewModel.userDobError,
                        icon: "calendar",
                        keyboardType: .numbersAndPunctuation
                    )

                    Spacer().frame(height: 4)

                    Button(action: { viewModel.requestPlaidLinkToken() }) {
                        if viewModel.isLoading {
                            ProgressView()
                                .tint(.white)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                        } else {
                            Text("Link Bank Account")
                                .font(.system(size: 16, weight: .semibold))
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 14)
                        }
                    }
                    .background(viewModel.isLinkFormValid && !viewModel.isLoading ? Color.xionOrange : Color.xionOrange.opacity(0.5))
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .disabled(!viewModel.isLinkFormValid || viewModel.isLoading)
                }
                .padding(16)
                .background(Color.cardBackground)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .shadow(color: Color.cardShadow, radius: 2, y: 1)

                if !viewModel.diagnostics.isEmpty {
                    Spacer().frame(height: 24)
                    PlaidDiagnosticsCard(viewModel: viewModel)
                }

                Spacer().frame(height: 32)
            }
            .padding(24)
        }
    }
}

// MARK: - Plaid Diagnostics Card

private struct PlaidDiagnosticsCard: View {
    @ObservedObject var viewModel: LinkBankViewModel
    @State private var copied = false

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Plaid Debug — last \(viewModel.diagnostics.count) session\(viewModel.diagnostics.count == 1 ? "" : "s")")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundStyle(Color.greetingText)
                Spacer()
                Button(copied ? "Copied!" : "Copy All") {
                    viewModel.copyDiagnosticsToClipboard()
                    copied = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { copied = false }
                }
                .font(.system(size: 12, weight: .semibold))
                .foregroundStyle(Color.xionOrange)

                Button("Clear") { viewModel.clearDiagnostics() }
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(Color.subtitleText)
            }

            ForEach(viewModel.diagnostics) { entry in
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(entry.outcome) — \(entry.phone)")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(Color.greetingText)

                    Text("token req_id: \(entry.tokenRequestId ?? "<not provided>")")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundStyle(Color.subtitleText)
                        .textSelection(.enabled)

                    Text("session_id:   \(entry.linkSessionId ?? "<not provided>")")
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundStyle(Color.subtitleText)
                        .textSelection(.enabled)

                    if let exitId = entry.exitRequestId {
                        Text("exit req_id:  \(exitId)")
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundStyle(Color.subtitleText)
                            .textSelection(.enabled)
                    }

                    if let err = entry.errorMessage {
                        Text("error: \(err)")
                            .font(.system(size: 10, design: .monospaced))
                            .foregroundStyle(.red)
                            .textSelection(.enabled)
                    }
                }
                .padding(8)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.screenBackground)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding(16)
        .background(Color.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: Color.cardShadow, radius: 2, y: 1)
    }
}

// MARK: - Text Field Component

private struct LinkBankTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    let error: String?
    let icon: String
    var keyboardType: UIKeyboardType = .default

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(spacing: 10) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundStyle(Color.subtitleText)
                    .frame(width: 20)

                TextField(placeholder, text: $text)
                    .font(.system(size: 15))
                    .keyboardType(keyboardType)
                    .autocapitalization(keyboardType == .emailAddress ? .none : .words)
                    .disableAutocorrection(true)
            }
            .padding(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(error != nil ? Color.red : Color(.systemGray4), lineWidth: 1)
            )

            if let error = error {
                Text(error)
                    .font(.system(size: 11))
                    .foregroundStyle(.red)
                    .padding(.leading, 4)
            }
        }
    }
}

// MARK: - Linked Success

private struct LinkedContent: View {
    @ObservedObject var viewModel: LinkBankViewModel
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Spacer().frame(height: 48)

                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 72))
                    .foregroundStyle(Color.xionGreen)

                Spacer().frame(height: 16)

                Text("Bank Account Linked")
                    .font(.system(size: 22, weight: .bold))
                    .foregroundStyle(Color.greetingText)

                if let name = viewModel.bankName {
                    Spacer().frame(height: 8)
                    Text(name)
                        .font(.system(size: 15))
                        .foregroundStyle(Color.subtitleText)
                }

                Spacer().frame(height: 32)

                VStack(spacing: 12) {
                    Button(action: { viewModel.unlinkBank() }) {
                        Text("Unlink")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.cardBackground)
                            .foregroundStyle(Color.subtitleText)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(.systemGray4), lineWidth: 1)
                            )
                    }

                    Button(action: onDone) {
                        Text("Done")
                            .font(.system(size: 16, weight: .semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(Color.xionOrange)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                .padding(.horizontal, 24)

                if !viewModel.diagnostics.isEmpty {
                    Spacer().frame(height: 24)
                    PlaidDiagnosticsCard(viewModel: viewModel)
                        .padding(.horizontal, 24)
                }

                Spacer().frame(height: 32)
            }
        }
    }
}
