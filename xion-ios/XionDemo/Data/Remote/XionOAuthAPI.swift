import Foundation

final class XionOAuthAPI {

    private let baseUrl: String

    init(baseUrl: String = Constants.restUrl) {
        self.baseUrl = baseUrl
    }

    func exchangeCode(
        code: String,
        codeVerifier: String,
        clientId: String
    ) async throws -> OAuthTokens {
        let url = URL(string: "\(baseUrl)oauth2/token")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let body = [
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": Constants.oauthRedirectUri,
            "code_verifier": codeVerifier,
            "client_id": clientId
        ]

        request.httpBody = body
            .map { "\($0.key)=\($0.value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? $0.value)" }
            .joined(separator: "&")
            .data(using: .utf8)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw APIError.requestFailed(statusCode: (response as? HTTPURLResponse)?.statusCode ?? 0)
        }

        return try JSONDecoder().decode(OAuthTokens.self, from: data)
    }

    func refreshToken(
        refreshToken: String,
        clientId: String
    ) async throws -> OAuthTokens {
        let url = URL(string: "\(baseUrl)oauth2/token")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/x-www-form-urlencoded", forHTTPHeaderField: "Content-Type")

        let body = [
            "grant_type": "refresh_token",
            "refresh_token": refreshToken,
            "client_id": clientId
        ]

        request.httpBody = body
            .map { "\($0.key)=\($0.value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? $0.value)" }
            .joined(separator: "&")
            .data(using: .utf8)

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw APIError.requestFailed(statusCode: (response as? HTTPURLResponse)?.statusCode ?? 0)
        }

        return try JSONDecoder().decode(OAuthTokens.self, from: data)
    }

    func getUserInfo(accessToken: String) async throws -> OAuthUserInfo {
        let url = URL(string: "\(baseUrl)api/v1/me")!
        var request = URLRequest(url: url)
        request.setValue("Bearer \(accessToken)", forHTTPHeaderField: "Authorization")

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw APIError.requestFailed(statusCode: (response as? HTTPURLResponse)?.statusCode ?? 0)
        }

        return try JSONDecoder().decode(OAuthUserInfo.self, from: data)
    }
}

enum APIError: LocalizedError {
    case requestFailed(statusCode: Int)

    var errorDescription: String? {
        switch self {
        case .requestFailed(let code): return "Request failed with status \(code)"
        }
    }
}
