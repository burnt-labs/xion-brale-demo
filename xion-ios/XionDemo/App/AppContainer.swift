import Foundation

extension Data {
    var hexEncoded: String {
        map { String(format: "%02x", $0) }.joined()
    }
}

final class AppContainer: ObservableObject {

    let secureStorage: SecureStorage
    let mobService: MobSigningServiceProtocol
    let oauthService: OAuthService
    let sessionManager: SessionManager
    let repository: XionRepositoryProtocol
    let braleService: BraleProxyService
    let braleRepository: BraleRepositoryProtocol
    let plaidLinkService: PlaidLinkService

    init() {
        secureStorage = SecureStorage()
        mobService = MobSigningService()
        oauthService = OAuthService()

        sessionManager = SessionManager(
            mobService: mobService,
            oauthService: oauthService,
            secureStorage: secureStorage
        )

        repository = XionRepositoryImpl(
            sessionManager: sessionManager,
            mobService: mobService
        )

        braleService = BraleProxyService(
            walletAddressProvider: { [weak sessionManager] in
                if case .connected(let addr, _, _, _, _) = sessionManager?.walletState {
                    return addr
                }
                return nil
            },
            authHeaderProvider: { [mobService] wallet in
                // Sign `xiondemo-auth:{wallet}:{unix_ts}` with the session key so the
                // proxy can verify wallet ownership and scope the response per-user.
                let timestamp = String(Int(Date().timeIntervalSince1970))
                let challenge = "xiondemo-auth:\(wallet):\(timestamp)"
                guard let data = challenge.data(using: .utf8),
                      let signed = mobService.signAuthChallenge(data) else { return nil }
                return [
                    "X-Auth-Timestamp": timestamp,
                    "X-Auth-Session-Address": signed.address,
                    "X-Auth-Signature": signed.signature.hexEncoded,
                ]
            }
        )
        braleRepository = BraleRepositoryImpl(
            braleService: braleService,
            secureStorage: secureStorage
        )
        plaidLinkService = PlaidLinkService()
    }
}
