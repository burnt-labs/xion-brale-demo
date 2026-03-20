import Foundation

enum ConnectionStep {
    case authenticating
    case generatingSessionKey
    case settingUpGrants
    case verifyingGrants
}

enum WalletState {
    case disconnected
    case connecting(step: ConnectionStep)
    case connected(
        metaAccountAddress: String,
        sessionKeyAddress: String,
        treasuryAddress: String,
        grantsActive: Bool,
        sessionExpiresAt: Int64
    )

    var isConnected: Bool {
        if case .connected = self { return true }
        return false
    }

    var metaAccountAddress: String? {
        if case .connected(let addr, _, _, _, _) = self { return addr }
        return nil
    }

    var sessionKeyAddress: String? {
        if case .connected(_, let addr, _, _, _) = self { return addr }
        return nil
    }

    var treasuryAddress: String? {
        if case .connected(_, _, let addr, _, _) = self { return addr }
        return nil
    }

    var grantsActive: Bool {
        if case .connected(_, _, _, let active, _) = self { return active }
        return false
    }

    var sessionExpiresAt: Int64 {
        if case .connected(_, _, _, _, let exp) = self { return exp }
        return 0
    }
}
