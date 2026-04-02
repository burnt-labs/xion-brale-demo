import Foundation

final class BraleProxyService {

    private let baseURL: String
    private let walletAddressProvider: () -> String?
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init(
        baseURL: String = Constants.braleProxyUrl,
        walletAddressProvider: @escaping () -> String? = { nil }
    ) {
        self.baseURL = baseURL.hasSuffix("/") ? String(baseURL.dropLast()) : baseURL
        self.walletAddressProvider = walletAddressProvider

        encoder = JSONEncoder()
        encoder.keyEncodingStrategy = .convertToSnakeCase

        decoder = JSONDecoder()
        decoder.keyDecodingStrategy = .convertFromSnakeCase
    }

    // MARK: - Plaid

    func createPlaidLinkToken(name: String, email: String) async throws -> PlaidLinkTokenResponse {
        let body = PlaidLinkTokenRequest(legalName: name, emailAddress: email)
        return try await post(path: "/plaid/link-token", body: body)
    }

    func registerBankAccount(publicToken: String) async throws -> String {
        let body = PlaidRegisterRequest(
            publicToken: publicToken,
            transferTypes: [Constants.braleAchDebitType, Constants.braleAchCreditType]
        )
        let response: PlaidRegisterResponse = try await post(path: "/plaid/register", body: body)
        return response.addressId
    }

    // MARK: - Addresses

    func getAddresses(type: String? = nil) async throws -> [BraleAddress] {
        var path = "/addresses"
        if let type = type {
            path += "?type=\(type)"
        }
        let response: AddressListResponse = try await get(path: path)
        return response.addresses
    }

    func createExternalAddress(request: CreateAddressRequest) async throws -> BraleAddress {
        try await post(path: "/addresses", body: request)
    }

    func getAddressBalance(id: String, transferType: String, valueType: String) async throws -> BraleBalance {
        let path = "/addresses/\(id)/balance?transfer_type=\(transferType)&value_type=\(valueType)"
        return try await get(path: path)
    }

    // MARK: - Transfers

    func createTransfer(request: CreateTransferRequest) async throws -> BraleTransfer {
        try await post(path: "/transfers", body: request)
    }

    func getTransfer(id: String) async throws -> BraleTransfer {
        try await get(path: "/transfers/\(id)")
    }

    func listTransfers() async throws -> [BraleTransfer] {
        let response: TransferListResponse = try await get(path: "/transfers")
        return response.transfers
    }

    // MARK: - Private Helpers

    private func get<T: Decodable>(path: String) async throws -> T {
        guard let url = URL(string: "\(baseURL)\(path)") else {
            throw BraleServiceError.invalidURL(path)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let walletAddress = walletAddressProvider() {
            request.setValue(walletAddress, forHTTPHeaderField: "X-Wallet-Address")
        }

        let (data, response) = try await URLSession.shared.data(for: request)
        try validateResponse(response, data: data)
        return try decoder.decode(T.self, from: data)
    }

    private func post<B: Encodable, T: Decodable>(path: String, body: B) async throws -> T {
        guard let url = URL(string: "\(baseURL)\(path)") else {
            throw BraleServiceError.invalidURL(path)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        if let walletAddress = walletAddressProvider() {
            request.setValue(walletAddress, forHTTPHeaderField: "X-Wallet-Address")
        }
        request.httpBody = try encoder.encode(body)

        let (data, response) = try await URLSession.shared.data(for: request)
        try validateResponse(response, data: data)
        return try decoder.decode(T.self, from: data)
    }

    private func validateResponse(_ response: URLResponse, data: Data) throws {
        guard let httpResponse = response as? HTTPURLResponse else {
            throw BraleServiceError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            let body = String(data: data, encoding: .utf8) ?? ""
            throw BraleServiceError.httpError(statusCode: httpResponse.statusCode, body: body)
        }
    }
}

// MARK: - Errors

enum BraleServiceError: LocalizedError {
    case invalidURL(String)
    case invalidResponse
    case httpError(statusCode: Int, body: String)

    var errorDescription: String? {
        switch self {
        case .invalidURL(let path):
            return "Invalid Brale API URL: \(path)"
        case .invalidResponse:
            return "Invalid response from Brale API"
        case .httpError(let statusCode, let body):
            return "Brale API error \(statusCode): \(body)"
        }
    }
}
