import Foundation

enum Constants {
    // Chain configuration
    static let rpcUrl = "https://rpc.xion-testnet-2.burnt.com:443"
    static let restUrl = "https://api.xion-testnet-2.burnt.com/"
    static let chainId = "xion-testnet-2"
    static let coinDenom = "uxion"
    static let displayDenom = "XION"
    static let gasPrice = "0.025"
    static let addressPrefix = "xion"
    static let decimals = 6

    // Session / Abstraxion
    static let treasuryAddress = "xion1sm3qp7kdqkkqgq5sdze6fjvk02a9psqqht2s575kdw06y4prlqcqhqa0mj"
    static let oauthClientId = ""
    static let oauthAuthorizationEndpoint = "https://auth.testnet.burnt.com/"
    static let sessionGrantDurationSeconds: Int64 = 86400

    // OAuth2
    static let oauthRedirectUri = "xiondemo://callback"
    static let oauthCallbackScheme = "xiondemo"

    // Keychain
    static let keychainService = "com.burnt.xiondemo.ios"
    static let keychainSessionMnemonic = "session_mnemonic"
    static let keychainMetaAccountAddress = "meta_account_address"
    static let keychainSessionKeyAddress = "session_key_address"
    static let keychainTreasuryAddress = "treasury_address"
    static let keychainSessionExpiry = "session_expiry"

    // Sample contract for demo
    static let sampleContractAddress = ""
    static let sampleContractMsg = #"{"increment": {}}"#
}
