import Foundation

final class AppContainer: ObservableObject {

    let secureStorage: SecureStorage
    let mobService: MobSigningServiceProtocol
    let oauthService: OAuthService
    let sessionManager: SessionManager
    let repository: XionRepositoryProtocol

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
    }
}
