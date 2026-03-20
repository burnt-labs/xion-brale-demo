import AuthenticationServices
import Foundation

final class OAuthService: NSObject, ASWebAuthenticationPresentationContextProviding {

    private var activeSession: ASWebAuthenticationSession?

    func startAuthFlow(granteeAddress: String) async throws -> AuthCallbackResult {
        var components = URLComponents(string: Constants.oauthAuthorizationEndpoint)!
        components.queryItems = [
            URLQueryItem(name: "treasury", value: Constants.treasuryAddress),
            URLQueryItem(name: "grantee", value: granteeAddress),
            URLQueryItem(name: "redirect_uri", value: Constants.oauthRedirectUri)
        ]

        guard let authUrl = components.url else {
            throw OAuthError.invalidUrl
        }

        let callbackUrl: URL = try await withCheckedThrowingContinuation { [weak self] continuation in
            let session = ASWebAuthenticationSession(
                url: authUrl,
                callbackURLScheme: Constants.oauthCallbackScheme
            ) { [weak self] url, error in
                self?.activeSession = nil
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let url = url {
                    continuation.resume(returning: url)
                } else {
                    continuation.resume(throwing: OAuthError.noCallback)
                }
            }

            session.presentationContextProvider = self
            session.prefersEphemeralWebBrowserSession = false

            self?.activeSession = session

            DispatchQueue.main.async {
                session.start()
            }
        }

        guard let components = URLComponents(url: callbackUrl, resolvingAgainstBaseURL: false),
              let address = components.queryItems?.first(where: { $0.name == "granter" })?.value else {
            throw OAuthError.noGranter
        }

        return AuthCallbackResult(metaAccountAddress: address)
    }

    // MARK: - ASWebAuthenticationPresentationContextProviding

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        ASPresentationAnchor()
    }
}

enum OAuthError: LocalizedError {
    case invalidUrl
    case noCallback
    case noGranter

    var errorDescription: String? {
        switch self {
        case .invalidUrl: return "Invalid auth URL"
        case .noCallback: return "No callback received"
        case .noGranter: return "No granter (meta account) address in callback"
        }
    }
}
