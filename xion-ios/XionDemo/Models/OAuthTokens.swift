import Foundation

struct OAuthTokens: Codable {
    let accessToken: String
    let tokenType: String
    let expiresIn: Int64
    let refreshToken: String?

    enum CodingKeys: String, CodingKey {
        case accessToken = "access_token"
        case tokenType = "token_type"
        case expiresIn = "expires_in"
        case refreshToken = "refresh_token"
    }
}

struct OAuthUserInfo: Codable {
    let address: String
    let chainId: String?

    enum CodingKeys: String, CodingKey {
        case address
        case chainId = "chain_id"
    }
}

struct AuthCallbackResult {
    let metaAccountAddress: String
}
