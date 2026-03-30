import Foundation

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

        braleService = BraleProxyService()
        braleRepository = BraleRepositoryImpl(
            braleService: braleService,
            secureStorage: secureStorage
        )
        plaidLinkService = PlaidLinkService()
    }
}
