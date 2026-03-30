import Foundation

protocol BraleRepositoryProtocol {
    func createPlaidLinkToken(name: String, email: String) async throws -> PlaidLinkTokenResponse
    func registerBankAccount(publicToken: String) async throws -> String
    func getInternalAddresses() async throws -> [BraleAddress]
    func findExistingBankAddress() async throws -> BraleAddress?
    func findExistingXionAddress(walletAddress: String) async throws -> BraleAddress?
    func registerXionAddress(walletAddress: String) async throws -> BraleAddress
    func createOnrampTransfer(amount: String, bankAddressId: String, xionAddressId: String) async throws -> BraleTransfer
    func createOfframpTransfer(amount: String, custodialAddressId: String, bankAddressId: String) async throws -> BraleTransfer
    func getTransfer(transferId: String) async throws -> BraleTransfer
}

final class BraleRepositoryImpl: BraleRepositoryProtocol {

    private let braleService: BraleProxyService
    private let secureStorage: SecureStorage

    init(braleService: BraleProxyService, secureStorage: SecureStorage) {
        self.braleService = braleService
        self.secureStorage = secureStorage
    }

    // MARK: - Plaid

    func createPlaidLinkToken(name: String, email: String) async throws -> PlaidLinkTokenResponse {
        try await braleService.createPlaidLinkToken(name: name, email: email)
    }

    func registerBankAccount(publicToken: String) async throws -> String {
        let addressId = try await braleService.registerBankAccount(publicToken: publicToken)
        secureStorage.saveBraleBankAddressId(addressId)
        return addressId
    }

    // MARK: - Addresses

    func getInternalAddresses() async throws -> [BraleAddress] {
        try await braleService.getAddresses(type: "internal")
    }

    func findExistingBankAddress() async throws -> BraleAddress? {
        // Check cached ID first
        if let cachedId = secureStorage.getBraleBankAddressId() {
            let addresses = try await braleService.getAddresses(type: "external")
            if let match = addresses.first(where: { $0.id == cachedId }) {
                return match
            }
        }

        // Search all external addresses for one with ACH debit capability
        let addresses = try await braleService.getAddresses(type: "external")
        let bankAddress = addresses.first { address in
            address.transferTypes?.contains(Constants.braleAchDebitType) == true
        }

        if let bankAddress = bankAddress {
            secureStorage.saveBraleBankAddressId(bankAddress.id)
        }

        return bankAddress
    }

    func findExistingXionAddress(walletAddress: String) async throws -> BraleAddress? {
        // Check cached ID first
        if let cachedId = secureStorage.getBraleXionAddressId() {
            let addresses = try await braleService.getAddresses(type: "external")
            if let match = addresses.first(where: { $0.id == cachedId }) {
                return match
            }
        }

        // Search for an external address matching the wallet address
        let addresses = try await braleService.getAddresses(type: "external")
        let xionAddress = addresses.first { address in
            address.address == walletAddress &&
            address.transferTypes?.contains(Constants.braleTransferType) == true
        }

        if let xionAddress = xionAddress {
            secureStorage.saveBraleXionAddressId(xionAddress.id)
        }

        return xionAddress
    }

    func registerXionAddress(walletAddress: String) async throws -> BraleAddress {
        let request = CreateAddressRequest(
            name: "XION Wallet",
            address: walletAddress,
            transferTypes: [Constants.braleTransferType]
        )
        let address = try await braleService.createExternalAddress(request: request)
        secureStorage.saveBraleXionAddressId(address.id)
        return address
    }

    // MARK: - Transfers

    func createOnrampTransfer(amount: String, bankAddressId: String, xionAddressId: String) async throws -> BraleTransfer {
        let request = CreateTransferRequest(
            amount: BraleAmount(value: amount, currency: Constants.braleFiatCurrency),
            source: BraleTransferEndpoint(
                addressId: bankAddressId,
                valueType: Constants.braleFiatValueType,
                transferType: Constants.braleAchDebitType,
                transactionId: nil
            ),
            destination: BraleTransferEndpoint(
                addressId: xionAddressId,
                valueType: Constants.braleStablecoinDenom,
                transferType: Constants.braleTransferType,
                transactionId: nil
            )
        )
        return try await braleService.createTransfer(request: request)
    }

    func createOfframpTransfer(amount: String, custodialAddressId: String, bankAddressId: String) async throws -> BraleTransfer {
        let request = CreateTransferRequest(
            amount: BraleAmount(value: amount, currency: Constants.braleFiatCurrency),
            source: BraleTransferEndpoint(
                addressId: custodialAddressId,
                valueType: Constants.braleStablecoinDenom,
                transferType: Constants.braleTransferType,
                transactionId: nil
            ),
            destination: BraleTransferEndpoint(
                addressId: bankAddressId,
                valueType: Constants.braleFiatValueType,
                transferType: Constants.braleAchCreditType,
                transactionId: nil
            )
        )
        return try await braleService.createTransfer(request: request)
    }

    func getTransfer(transferId: String) async throws -> BraleTransfer {
        try await braleService.getTransfer(id: transferId)
    }
}
