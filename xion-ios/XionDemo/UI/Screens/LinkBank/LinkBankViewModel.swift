import Foundation

@MainActor
final class LinkBankViewModel: ObservableObject {

    // Form fields
    @Published var userName = ""
    @Published var userEmail = ""
    @Published var userPhone = ""
    @Published var userDob = ""
    @Published var userNameError: String?
    @Published var userEmailError: String?
    @Published var userPhoneError: String?
    @Published var userDobError: String?

    // Bank state
    @Published var bankLinked = false
    @Published var bankAddressId: String?
    @Published var bankName: String?
    @Published var isLoading = false
    @Published var error: String?

    private let braleRepository: BraleRepositoryProtocol
    private let secureStorage: SecureStorage
    private let plaidLinkService: PlaidLinkService

    var isLinkFormValid: Bool {
        !userName.trimmingCharacters(in: .whitespaces).isEmpty
            && userNameError == nil
            && !userEmail.trimmingCharacters(in: .whitespaces).isEmpty
            && userEmailError == nil
            && !userPhone.trimmingCharacters(in: .whitespaces).isEmpty
            && userPhoneError == nil
            && !userDob.trimmingCharacters(in: .whitespaces).isEmpty
            && userDobError == nil
    }

    init(
        braleRepository: BraleRepositoryProtocol,
        secureStorage: SecureStorage,
        plaidLinkService: PlaidLinkService
    ) {
        self.braleRepository = braleRepository
        self.secureStorage = secureStorage
        self.plaidLinkService = plaidLinkService

        // Restore cached bank ID and user data from secure storage
        let bankId = secureStorage.getBraleBankAddressId()
        bankLinked = bankId != nil && !bankId!.isEmpty
        bankAddressId = bankId
        userName = secureStorage.getBraleUserName() ?? ""
        userEmail = secureStorage.getBraleUserEmail() ?? ""
        userPhone = secureStorage.getBraleUserPhone() ?? ""
        userDob = secureStorage.getBraleUserDob() ?? ""
    }

    // MARK: - Validation

    func updateUserName(_ value: String) {
        userName = value
        userNameError = value.trimmingCharacters(in: .whitespaces).isEmpty ? "Name is required" : nil
    }

    func updateUserEmail(_ value: String) {
        userEmail = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            userEmailError = "Email is required"
        } else if !trimmed.contains("@") || !trimmed.contains(".") {
            userEmailError = "Enter a valid email address"
        } else {
            userEmailError = nil
        }
    }

    func updateUserPhone(_ value: String) {
        userPhone = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            userPhoneError = "Phone number is required"
        } else if !trimmed.hasPrefix("+") {
            userPhoneError = "Must start with + (e.g. +15551234567)"
        } else if !trimmed.dropFirst().allSatisfy(\.isNumber) {
            userPhoneError = "Only digits after +"
        } else if trimmed.count < 11 {
            userPhoneError = "Enter full number with country code"
        } else {
            userPhoneError = nil
        }
    }

    func updateUserDob(_ value: String) {
        userDob = value
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        if trimmed.isEmpty {
            userDobError = "Date of birth is required"
        } else if trimmed.range(of: #"^\d{4}-\d{2}-\d{2}$"#, options: .regularExpression) == nil {
            userDobError = "Use YYYY-MM-DD format"
        } else {
            userDobError = nil
        }
    }

    // MARK: - Plaid Link

    func requestPlaidLinkToken() {
        let name = userName.trimmingCharacters(in: .whitespaces)
        let email = userEmail.trimmingCharacters(in: .whitespaces)
        let phone = userPhone.trimmingCharacters(in: .whitespaces)
        let dob = userDob.trimmingCharacters(in: .whitespaces)

        // Persist user data
        secureStorage.saveBraleUserName(name)
        secureStorage.saveBraleUserEmail(email)
        secureStorage.saveBraleUserPhone(phone)
        secureStorage.saveBraleUserDob(dob)

        Task {
            isLoading = true
            error = nil
            do {
                let response = try await braleRepository.createPlaidLinkToken(
                    name: name, email: email, phone: phone, dob: dob
                )
                let result = try await plaidLinkService.openLink(token: response.linkToken)
                switch result {
                case .success(let publicToken):
                    onPlaidSuccess(publicToken: publicToken)
                case .cancelled:
                    onPlaidCancelled()
                }
            } catch {
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }

    func onPlaidSuccess(publicToken: String) {
        Task {
            isLoading = true
            error = nil
            do {
                let addressId = try await braleRepository.registerBankAccount(publicToken: publicToken)
                secureStorage.saveBraleBankAddressId(addressId)
                bankLinked = true
                bankAddressId = addressId
                isLoading = false
            } catch {
                self.error = error.localizedDescription
                isLoading = false
            }
        }
    }

    func onPlaidCancelled() {
        isLoading = false
    }

    func unlinkBank() {
        secureStorage.deleteBraleBankAddressId()
        bankLinked = false
        bankAddressId = nil
        bankName = nil
    }

    func clearError() {
        error = nil
    }
}
