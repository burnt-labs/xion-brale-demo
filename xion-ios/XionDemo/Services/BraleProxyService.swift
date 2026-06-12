import Foundation

final class BraleProxyService {

    private let baseURL: String
    private let walletAddressProvider: () -> String?
    private let authHeaderProvider: (_ wallet: String, _ method: String, _ path: String) -> [String: String]?
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init(
        baseURL: String = Constants.braleProxyUrl,
        walletAddressProvider: @escaping () -> String? = { nil },
        authHeaderProvider: @escaping (_ wallet: String, _ method: String, _ path: String) -> [String: String]? = { _, _, _ in nil }
    ) {
        self.baseURL = baseURL.hasSuffix("/") ? String(baseURL.dropLast()) : baseURL
        self.walletAddressProvider = walletAddressProvider
        self.authHeaderProvider = authHeaderProvider

        encoder = JSONEncoder()
        encoder.keyEncodingStrategy = .convertToSnakeCase

        decoder = JSONDecoder()
        decoder.keyDecodingStrategy = .convertFromSnakeCase
    }

    // MARK: - Plaid

    func createPlaidLinkToken(name: String, email: String, phone: String?, dob: String?) async throws -> PlaidLinkTokenResponse {
        func nilIfEmpty(_ value: String?) -> String? {
            guard let value, !value.trimmingCharacters(in: .whitespaces).isEmpty else { return nil }
            return value
        }
        let body = PlaidLinkTokenRequest(
            legalName: nilIfEmpty(name),
            emailAddress: nilIfEmpty(email),
            phoneNumber: nilIfEmpty(phone),
            dateOfBirth: nilIfEmpty(dob)
        )
        return try await post(path: "/plaid/link-token", body: body)
    }

    func registerBankAccount(publicToken: String, accountMask: String?) async throws -> String {
        let body = PlaidRegisterRequest(
            publicToken: publicToken,
            transferTypes: [Constants.braleAchDebitType, Constants.braleAchCreditType],
            accountMask: accountMask
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
        try await post(path: "/addresses/external", body: request)
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

    /// Attaches X-Wallet-Address plus signed auth headers (timestamp, pubkey,
    /// signature) so the proxy can verify wallet ownership and scope the response.
    private func applyWalletHeaders(_ request: inout URLRequest) {
        guard let walletAddress = walletAddressProvider() else { return }
        request.setValue(walletAddress, forHTTPHeaderField: "X-Wallet-Address")
        // Sign the method + path (matching the proxy's `c.req.path`, query excluded)
        // so the signature is bound to this specific request.
        let method = request.httpMethod ?? "GET"
        let path = request.url?.path ?? ""
        guard let headers = authHeaderProvider(walletAddress, method, path) else { return }
        for (key, value) in headers {
            request.setValue(value, forHTTPHeaderField: key)
        }
    }

    private func get<T: Decodable>(path: String) async throws -> T {
        guard let url = URL(string: "\(baseURL)\(path)") else {
            throw BraleServiceError.invalidURL(path)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        applyWalletHeaders(&request)

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
        applyWalletHeaders(&request)
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
            if let parsed = parseBraleErrorBody(body) {
                return parsed
            }
            return "Brale API error \(statusCode)"
        }
    }
}

/// Parse the brale-proxy error envelope and extract a user-friendly field message.
/// Brale wraps Plaid validation errors as:
///   { "error": "Brale API error (422)",
///     "details": { "code": "...", "detail": "{\"phone_numbers\":{\"0\":{\"phone_number\":\"...\"}}}" } }
/// We dig into `details.detail` (a JSON-encoded string), parse it, and return the first
/// "field: message" we find, or fall back to the outer error string.
private func parseBraleErrorBody(_ body: String) -> String? {
    guard let bodyData = body.data(using: .utf8) else { return nil }
    guard let outer = try? JSONSerialization.jsonObject(with: bodyData) as? [String: Any] else {
        return nil
    }
    if let details = outer["details"] as? [String: Any] {
        if let detailString = details["detail"] as? String {
            if let detailData = detailString.data(using: .utf8),
               let detailJson = try? JSONSerialization.jsonObject(with: detailData),
               let leaf = findFirstFieldMessage(detailJson) {
                return leaf
            }
            return detailString
        }
        if let leaf = findFirstFieldMessage(details) {
            return leaf
        }
    }
    return outer["error"] as? String
}

private func findFirstFieldMessage(_ obj: Any, lastKey: String? = nil) -> String? {
    if let str = obj as? String, let key = lastKey {
        return "\(key): \(str)"
    }
    if let dict = obj as? [String: Any] {
        for (key, value) in dict {
            if let result = findFirstFieldMessage(value, lastKey: key) {
                return result
            }
        }
    }
    if let arr = obj as? [Any] {
        for value in arr {
            if let result = findFirstFieldMessage(value, lastKey: lastKey) {
                return result
            }
        }
    }
    return nil
}
