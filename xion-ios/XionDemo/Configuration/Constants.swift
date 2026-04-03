import Foundation

enum Constants {
    // Chain configuration
    // To override: edit these values or load from Info.plist / xcconfig
    static let rpcUrl = envString("XION_RPC_URL") ?? "https://rpc.xion-testnet-2.burnt.com:443"
    static let restUrl = envString("XION_REST_URL") ?? "https://api.xion-testnet-2.burnt.com/"
    static let chainId = envString("XION_CHAIN_ID") ?? "xion-testnet-2"
    static let coinDenom = "uxion"
    static let displayDenom = "XION"
    static let gasPrice = "0.025"
    static let addressPrefix = "xion"
    static let decimals = 6
    static let coinType: UInt32 = 118
    static let derivationPath = "m/44'/118'/0'/0/0"

    // Session / Abstraxion
    static let treasuryAddress = envString("XION_TREASURY_ADDRESS") ?? "xion1rytzathz8y2r58lj26ls3z90tn475qdtpet58nc98v0mur78g4yqvm44qk"
    static let oauthClientId = envString("XION_OAUTH_CLIENT_ID") ?? ""
    static let oauthAuthorizationEndpoint = envString("XION_OAUTH_AUTHORIZATION_ENDPOINT") ?? "https://auth.testnet.burnt.com/"
    static let sessionGrantDurationSeconds: Int64 = 86400

    // OAuth2
    static let oauthRedirectUri = "xiondemo://callback"
    static let oauthCallbackScheme = "xiondemo"

    // Brale onramp/offramp
    static let braleProxyUrl = envString("BRALE_PROXY_URL") ?? "http://localhost:3000/"
    static let braleTransferType = "xion_testnet"
    static let braleStablecoinDenom = "SBC"
    static let braleSbcOnChainDenom = "factory/xion17grq736740r70awldugfs3mls3stu9haewctv2/sbc"
    static let braleAchDebitType = "ach_debit"
    static let braleAchCreditType = "same_day_ach_credit"
    static let braleFiatValueType = "USD"
    static let braleFiatCurrency = "USD"
    static let sbcDisplayDenom = "SBC"
    static let sbcDecimals = 6

    // Keychain
    static let keychainService = "com.burnt.xiondemo.ios"
    static let keychainSessionMnemonic = "session_mnemonic"
    static let keychainMetaAccountAddress = "meta_account_address"
    static let keychainSessionKeyAddress = "session_key_address"
    static let keychainTreasuryAddress = "treasury_address"
    static let keychainSessionExpiry = "session_expiry"
    // Brale keychain keys
    static let keychainBraleBankAddressId = "brale_bank_address_id"
    static let keychainBraleXionAddressId = "brale_xion_address_id"
    static let keychainBraleUserName = "brale_user_name"
    static let keychainBraleUserEmail = "brale_user_email"
    static let keychainBraleUserPhone = "brale_user_phone"
    static let keychainBraleUserDob = "brale_user_dob"

    // Sample contract for demo
    static let sampleContractAddress = ""
    static let sampleContractMsg = #"{"increment": {}}"#

    // Vault contract (testnet)
    static let vaultContractAddress = "xion1snjtcrvqtlpmzkxfwwt69nffwz8tqazl3t4r38tqllvx3qx7redsmhcvgl"

    // Read a value from Info.plist (set via xcconfig or build settings)
    private static func envString(_ key: String) -> String? {
        guard let value = Bundle.main.infoDictionary?[key] as? String,
              !value.isEmpty, !value.hasPrefix("$") else {
            return nil
        }
        return value
    }
}
