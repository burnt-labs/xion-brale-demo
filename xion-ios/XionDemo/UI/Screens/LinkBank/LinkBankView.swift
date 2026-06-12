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
                    .onAppear { viewModel.loadExistingBanks() }
            }

            // Error overlay at top
            VStack {
                ErrorBanner(message: viewModel.error, onDismiss: viewModel.clearError)
                    .padding(.horizontal, 24)
                    .padding(.top, 8)
                Spacer()
            }

            // Full-screen loading — covers the gap when returning from Plaid while
            // the bank registers, which a button spinner alone didn't make clear.
            LoadingOverlay(isVisible: viewModel.isLoading, message: "Linking your bank account…")
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
                // Existing linked banks — skeleton while loading (can take a while),
                // then the real list, then the option to link a new one via Plaid.
                if viewModel.isLoadingBanks {
                    BankSkeletonCard()
                    Spacer().frame(height: 24)
                } else if !viewModel.existingBanks.isEmpty {
                    ExistingBanksCard(viewModel: viewModel)
                    Spacer().frame(height: 24)
                }

                if !viewModel.isLoadingBanks {
                    Text(viewModel.existingBanks.isEmpty ? "Link a bank" : "Or link a new bank")
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Color.greetingText)
                    Spacer().frame(height: 8)

                    VStack(alignment: .leading, spacing: 12) {
                        Text("Enter your phone number to connect a new bank through Plaid.")
                            .font(.system(size: 13))
                            .foregroundStyle(Color.subtitleText)

                        VStack(alignment: .leading, spacing: 4) {
                            HStack(spacing: 8) {
                                Text("🇺🇸 +1")
                                    .font(.system(size: 15, weight: .medium))
                                    .foregroundStyle(Color.greetingText)
                                Rectangle()
                                    .fill(Color(.systemGray4))
                                    .frame(width: 1, height: 22)
                                TextField("713 555 0199", text: Binding(
                                    get: {
                                        let p = viewModel.userPhone
                                        return p.hasPrefix("+1") ? String(p.dropFirst(2)) : p
                                    },
                                    set: { local in
                                        let digits = String(local.filter(\.isNumber).prefix(10))
                                        viewModel.updateUserPhone(digits.isEmpty ? "" : "+1" + digits)
                                    }
                                ))
                                .font(.system(size: 15))
                                .keyboardType(.numberPad)
                                .disableAutocorrection(true)
                            }
                            .padding(12)
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(viewModel.userPhoneError != nil ? Color.red : Color(.systemGray4), lineWidth: 1)
                            )
                            if let phoneError = viewModel.userPhoneError {
                                Text(phoneError)
                                    .font(.system(size: 11))
                                    .foregroundStyle(.red)
                                    .padding(.leading, 4)
                            }
                        }

                        Button(action: { viewModel.requestPlaidLinkToken() }) {
                            Group {
                                if viewModel.isLoading {
                                    ProgressView().tint(.white)
                                } else {
                                    Text("Link New Bank Account")
                                        .font(.system(size: 16, weight: .semibold))
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(viewModel.isPhoneValid && !viewModel.isLoading ? Color.xionOrange : Color.xionOrange.opacity(0.5))
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                        .disabled(!viewModel.isPhoneValid || viewModel.isLoading)
                    }
                    .padding(16)
                    .background(Color.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: Color.cardShadow, radius: 2, y: 1)
                }

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

// MARK: - Loading Skeleton

private struct BankSkeletonCard: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Use a linked bank")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(Color.greetingText)

            ForEach(0..<2, id: \.self) { _ in
                HStack(spacing: 12) {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(Color.subtitleText.opacity(0.15))
                        .frame(width: 24, height: 24)
                    VStack(alignment: .leading, spacing: 6) {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.subtitleText.opacity(0.15))
                            .frame(width: 150, height: 13)
                        RoundedRectangle(cornerRadius: 4)
                            .fill(Color.subtitleText.opacity(0.10))
                            .frame(width: 110, height: 11)
                    }
                    Spacer()
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.screenBackground)
                .clipShape(RoundedRectangle(cornerRadius: 10))
            }
        }
        .padding(16)
        .background(Color.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: Color.cardShadow, radius: 2, y: 1)
    }
}

// MARK: - Existing Banks Card

private struct ExistingBanksCard: View {
    @ObservedObject var viewModel: LinkBankViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Use a linked bank")
                .font(.system(size: 14, weight: .semibold))
                .foregroundStyle(Color.greetingText)

            ForEach(viewModel.existingBanks) { bank in
                Button(action: { viewModel.useExistingBank(bank) }) {
                    HStack(spacing: 12) {
                        Image(systemName: "building.columns.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(Color.xionOrange)
                            .frame(width: 24)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(bank.name ?? "Bank account")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(Color.greetingText)
                            if let detail = subtitle(for: bank) {
                                Text(detail)
                                    .font(.system(size: 12))
                                    .foregroundStyle(Color.subtitleText)
                            }
                        }

                        Spacer()

                        Image(systemName: "chevron.right")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(Color.subtitleText)
                    }
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.screenBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
                }
                .buttonStyle(.plain)
            }
        }
        .padding(16)
        .background(Color.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: Color.cardShadow, radius: 2, y: 1)
    }

    private func subtitle(for bank: BraleAddress) -> String? {
        var parts: [String] = []
        if let owner = bank.owner { parts.append(owner) }
        if let acct = bank.accountNumber { parts.append(acct) }
        return parts.isEmpty ? nil : parts.joined(separator: " · ")
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
                    Spacer().frame(height: 16)
                    HStack(spacing: 12) {
                        Image(systemName: "building.columns.fill")
                            .font(.system(size: 18))
                            .foregroundStyle(Color.xionOrange)
                        Text(name)
                            .font(.system(size: 16, weight: .medium))
                            .foregroundStyle(Color.greetingText)
                    }
                    .padding(.vertical, 12)
                    .padding(.horizontal, 16)
                    .frame(maxWidth: .infinity)
                    .background(Color.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: Color.cardShadow, radius: 2, y: 1)
                    .padding(.horizontal, 24)
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
