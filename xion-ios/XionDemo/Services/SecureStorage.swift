import Foundation
import Security

final class SecureStorage {

    private let service = Constants.keychainService

    func saveSessionMnemonic(_ mnemonic: String) {
        save(key: Constants.keychainSessionMnemonic, value: mnemonic)
    }

    func getSessionMnemonic() -> String? {
        load(key: Constants.keychainSessionMnemonic)
    }

    func saveMetaAccountAddress(_ address: String) {
        save(key: Constants.keychainMetaAccountAddress, value: address)
    }

    func getMetaAccountAddress() -> String? {
        load(key: Constants.keychainMetaAccountAddress)
    }

    func saveSessionKeyAddress(_ address: String) {
        save(key: Constants.keychainSessionKeyAddress, value: address)
    }

    func getSessionKeyAddress() -> String? {
        load(key: Constants.keychainSessionKeyAddress)
    }

    func saveTreasuryAddress(_ address: String) {
        save(key: Constants.keychainTreasuryAddress, value: address)
    }

    func getTreasuryAddress() -> String? {
        load(key: Constants.keychainTreasuryAddress)
    }

    func saveSessionExpiry(_ expiresAt: Int64) {
        save(key: Constants.keychainSessionExpiry, value: String(expiresAt))
    }

    func getSessionExpiry() -> Int64 {
        guard let value = load(key: Constants.keychainSessionExpiry) else { return 0 }
        return Int64(value) ?? 0
    }

    func saveSessionData(
        sessionMnemonic: String,
        metaAccountAddress: String,
        sessionKeyAddress: String,
        treasuryAddress: String
    ) {
        saveSessionMnemonic(sessionMnemonic)
        saveMetaAccountAddress(metaAccountAddress)
        saveSessionKeyAddress(sessionKeyAddress)
        saveTreasuryAddress(treasuryAddress)
    }

    func clearAll() {
        let keys = [
            Constants.keychainSessionMnemonic,
            Constants.keychainMetaAccountAddress,
            Constants.keychainSessionKeyAddress,
            Constants.keychainTreasuryAddress,
            Constants.keychainSessionExpiry
        ]
        for key in keys {
            delete(key: key)
        }
    }

    // MARK: - Private

    private func save(key: String, value: String) {
        guard let data = value.data(using: .utf8) else { return }
        delete(key: key)

        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ]

        SecItemAdd(query as CFDictionary, nil)
    }

    private func load(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]

        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)

        guard status == errSecSuccess, let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
