import Combine
import Foundation
import Bip39

final class SessionManager: ObservableObject {

    @Published private(set) var walletState: WalletState = .disconnected
    @Published private(set) var transactionHistory: [TransactionResult] = []

    private let mobService: MobSigningServiceProtocol
    private let oauthService: OAuthService
    private let secureStorage: SecureStorage

    init(
        mobService: MobSigningServiceProtocol,
        oauthService: OAuthService,
        secureStorage: SecureStorage
    ) {
        self.mobService = mobService
        self.oauthService = oauthService
        self.secureStorage = secureStorage
    }

    @MainActor
    func authenticate() async throws -> String {
        // Step 1: Generate session key BEFORE launching auth
        walletState = .connecting(step: .generatingSessionKey)

        let mnemonic = try Mnemonic(strength: 256)
        let sessionMnemonic = mnemonic.mnemonic().joined(separator: " ")

        let sessionKeyAddress = try await mobService.createClientWithSigner(mnemonic: sessionMnemonic)

        // Step 2: Launch abstraxion dashboard with grantee address
        walletState = .connecting(step: .authenticating)

        let callbackResult = try await oauthService.startAuthFlow(granteeAddress: sessionKeyAddress)
        let metaAccountAddress = callbackResult.metaAccountAddress

        // Step 3: Save session data
        walletState = .connecting(step: .settingUpGrants)

        secureStorage.saveSessionData(
            sessionMnemonic: sessionMnemonic,
            metaAccountAddress: metaAccountAddress,
            sessionKeyAddress: sessionKeyAddress,
            treasuryAddress: Constants.treasuryAddress
        )

        let expiresAt = Int64(Date().timeIntervalSince1970) + Constants.sessionGrantDurationSeconds
        secureStorage.saveSessionExpiry(expiresAt)

        // Upgrade to session client so granter/feeGranter are handled internally
        try await mobService.upgradeToSessionClient(
            metaAccountAddress: metaAccountAddress,
            treasuryAddress: Constants.treasuryAddress,
            sessionExpiresAt: expiresAt
        )

        walletState = .connecting(step: .verifyingGrants)

        // Poll LCD to verify grants are active
        let grantsFound = await pollForGrants(granter: metaAccountAddress, grantee: sessionKeyAddress)

        walletState = .connected(
            metaAccountAddress: metaAccountAddress,
            sessionKeyAddress: sessionKeyAddress,
            treasuryAddress: Constants.treasuryAddress,
            grantsActive: grantsFound,
            sessionExpiresAt: expiresAt
        )

        return metaAccountAddress
    }

    @MainActor
    func restoreSession() async -> Bool {
        guard let sessionMnemonic = secureStorage.getSessionMnemonic(),
              let metaAccountAddress = secureStorage.getMetaAccountAddress(),
              let sessionKeyAddress = secureStorage.getSessionKeyAddress(),
              let treasuryAddress = secureStorage.getTreasuryAddress() else {
            return false
        }

        let expiresAt = secureStorage.getSessionExpiry()
        let now = Int64(Date().timeIntervalSince1970)
        if expiresAt > 0 && now >= expiresAt {
            secureStorage.clearAll()
            return false
        }

        do {
            walletState = .connecting(step: .generatingSessionKey)

            _ = try await mobService.createClientWithSigner(mnemonic: sessionMnemonic)

            // Upgrade to session client so granter/feeGranter are handled internally
            try await mobService.upgradeToSessionClient(
                metaAccountAddress: metaAccountAddress,
                treasuryAddress: treasuryAddress,
                sessionExpiresAt: expiresAt
            )

            walletState = .connected(
                metaAccountAddress: metaAccountAddress,
                sessionKeyAddress: sessionKeyAddress,
                treasuryAddress: treasuryAddress,
                grantsActive: true,
                sessionExpiresAt: expiresAt
            )

            return true
        } catch {
            walletState = .disconnected
            return false
        }
    }

    @MainActor
    func disconnect() {
        mobService.disconnect()
        secureStorage.clearAll()
        walletState = .disconnected
        transactionHistory = []
    }

    @MainActor
    func setGrantsActive(_ active: Bool) {
        if case .connected(let meta, let session, let treasury, _, let expires) = walletState {
            walletState = .connected(
                metaAccountAddress: meta,
                sessionKeyAddress: session,
                treasuryAddress: treasury,
                grantsActive: active,
                sessionExpiresAt: expires
            )
        }
    }

    func appendTransaction(_ tx: TransactionResult) {
        DispatchQueue.main.async {
            self.transactionHistory.append(tx)
        }
    }

    // MARK: - Grant Polling

    private func pollForGrants(granter: String, grantee: String, maxAttempts: Int = 5) async -> Bool {
        var delayNs: UInt64 = 2_000_000_000 // 2 seconds
        for _ in 0..<maxAttempts {
            do {
                let urlString = "\(Constants.restUrl)cosmos/authz/v1beta1/grants?granter=\(granter)&grantee=\(grantee)"
                guard let url = URL(string: urlString) else { continue }
                let data = try await NativeHttpTransport.get(url: urlString)
                if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
                   let grants = json["grants"] as? [[String: Any]],
                   !grants.isEmpty {
                    return true
                }
            } catch {
                // Network error, retry
            }
            try? await Task.sleep(nanoseconds: delayNs)
            delayNs = min(delayNs * 2, 16_000_000_000)
        }
        return false
    }
}
