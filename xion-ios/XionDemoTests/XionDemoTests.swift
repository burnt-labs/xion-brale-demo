import XCTest
@testable import XionDemo

final class CoinFormatterTests: XCTestCase {

    func testMicroToDisplay() {
        XCTAssertEqual(CoinFormatter.microToDisplay("1000000"), "1")
        XCTAssertEqual(CoinFormatter.microToDisplay("500000"), "0.5")
        XCTAssertEqual(CoinFormatter.microToDisplay("0"), "0")
    }

    func testDisplayToMicro() {
        XCTAssertEqual(CoinFormatter.displayToMicro("1"), "1000000")
        XCTAssertEqual(CoinFormatter.displayToMicro("0.5"), "500000")
        XCTAssertEqual(CoinFormatter.displayToMicro("0"), "0")
    }

    func testIsValidAmount() {
        XCTAssertTrue(CoinFormatter.isValidAmount("1.5"))
        XCTAssertTrue(CoinFormatter.isValidAmount("100"))
        XCTAssertFalse(CoinFormatter.isValidAmount("0"))
        XCTAssertFalse(CoinFormatter.isValidAmount("-1"))
        XCTAssertFalse(CoinFormatter.isValidAmount("abc"))
        XCTAssertFalse(CoinFormatter.isValidAmount(""))
    }
}

final class PkceUtilTests: XCTestCase {

    func testCodeVerifierLength() {
        let verifier = PkceUtil.generateCodeVerifier()
        XCTAssertGreaterThanOrEqual(verifier.count, 43)
        XCTAssertLessThanOrEqual(verifier.count, 128)
    }

    func testCodeVerifierRandomness() {
        let v1 = PkceUtil.generateCodeVerifier()
        let v2 = PkceUtil.generateCodeVerifier()
        XCTAssertNotEqual(v1, v2)
    }

    func testCodeChallengeIsDeterministic() {
        let verifier = "test_verifier_string"
        let c1 = PkceUtil.generateCodeChallenge(from: verifier)
        let c2 = PkceUtil.generateCodeChallenge(from: verifier)
        XCTAssertEqual(c1, c2)
    }

    func testCodeChallengeIsBase64UrlEncoded() {
        let verifier = PkceUtil.generateCodeVerifier()
        let challenge = PkceUtil.generateCodeChallenge(from: verifier)
        XCTAssertFalse(challenge.contains("+"))
        XCTAssertFalse(challenge.contains("/"))
        XCTAssertFalse(challenge.contains("="))
    }
}

final class SecureStorageTests: XCTestCase {

    private var storage: SecureStorage!

    override func setUp() {
        super.setUp()
        storage = SecureStorage()
        storage.clearAll()
    }

    override func tearDown() {
        storage.clearAll()
        super.tearDown()
    }

    func testSaveAndRetrieveMnemonic() {
        let mnemonic = "test mnemonic phrase for secure storage"
        storage.saveSessionMnemonic(mnemonic)
        XCTAssertEqual(storage.getSessionMnemonic(), mnemonic)
    }

    func testClearAll() {
        storage.saveSessionMnemonic("test")
        storage.saveMetaAccountAddress("xion1test")
        storage.clearAll()
        XCTAssertNil(storage.getSessionMnemonic())
        XCTAssertNil(storage.getMetaAccountAddress())
    }

    func testSessionExpiry() {
        let expiry: Int64 = 1234567890
        storage.saveSessionExpiry(expiry)
        XCTAssertEqual(storage.getSessionExpiry(), expiry)
    }
}
