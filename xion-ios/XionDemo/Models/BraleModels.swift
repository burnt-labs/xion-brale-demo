import Foundation

// MARK: - Address Models

struct BraleAddress: Codable, Identifiable {
    let id: String
    let name: String?
    let type: String?
    let address: String?
    let status: String?
    let transferTypes: [String]?
}

struct AddressListResponse: Codable {
    let addresses: [BraleAddress]
}

// MARK: - Transfer Models

struct BraleAmount: Codable {
    let value: String
    let currency: String
}

struct BraleTransferEndpoint: Codable {
    let addressId: String
    let valueType: String
    let transferType: String
    let transactionId: String?
}

struct BraleTransfer: Codable, Identifiable {
    let id: String
    let status: String
    let amount: BraleAmount
    let source: BraleTransferEndpoint?
    let destination: BraleTransferEndpoint?
    let createdAt: String?
    let updatedAt: String?
}

struct TransferListResponse: Codable {
    let transfers: [BraleTransfer]
}

// MARK: - Request Models

struct CreateAddressRequest: Codable {
    let name: String
    let address: String?
    let transferTypes: [String]
}

struct CreateTransferRequest: Codable {
    let amount: BraleAmount
    let source: BraleTransferEndpoint
    let destination: BraleTransferEndpoint
}

// MARK: - Plaid Models

struct PlaidLinkTokenRequest: Codable {
    let legalName: String
    let emailAddress: String
}

struct PlaidLinkTokenResponse: Codable {
    let linkToken: String
    let expiration: String?
}

struct PlaidRegisterRequest: Codable {
    let publicToken: String
    let transferTypes: [String]
}

struct PlaidRegisterResponse: Codable {
    let addressId: String
}

// MARK: - Balance Model

struct BraleBalance: Codable {
    let available: String?
    let pending: String?
}
