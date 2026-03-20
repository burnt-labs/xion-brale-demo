//
//  SHA2.swift
//
//
//  Created by Yehor Popovych on 20.05.2021.
//

import XCTest
@testable import UncommonCrypto

final class SHA2Tests: XCTestCase {
    let testCases256: [(input: Data, hash: String)] = [
        ("test1".utf8, "1b4f0e9851971998e732078544c96b36c3d01cedf7caa332359d6f1d83567014"),
        (Data(), "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
        ("Some data for hashing".utf8, "4e566898df6269c8321a040800af25ba1a2a67ca43cd68257ded0cdd402de1da")
    ]
    
    let testCases512: [(input: Data, hash: String)] = [
        ("test1".utf8, "b16ed7d24b3ecbd4164dcdad374e08c0ab7518aa07f9d3683f34c2b3c67a15830268cb4a56c1ff6f54c8e54a795f5b87c08668b51f82d0093f7baee7d2981181"),
        (Data(), "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"),
        ("Some data for hashing".utf8, "bbba4c51781977ca4dd0d9f0d1dcf49cb02021e288f98f45cb24bc61be6d92da7e31c825fdb034f251dd850131566b1c2b7e89d4f1b061169d85f7c8e96b2809")
    ]
    
    func test256StreamingDataApi() {
        var sha2 = SHA2(type: .sha256)
        for (input, expect) in testCases256 {
            sha2.update(input)
            XCTAssertEqual(sha2.finalize().hex, expect)
            sha2.reset()
        }
    }
    
    func test256StreamingBytesApi() {
        var sha2 = SHA2(type: .sha256)
        for (input, expect) in testCases256 {
            sha2.update(Array(input))
            XCTAssertEqual(sha2.finalize().hex, expect)
            sha2.reset()
        }
    }
    
    func test256StaticDataApi() {
        for (input, expect) in testCases256 {
            let hash = SHA2.hash(type: .sha256, data: input)
            XCTAssertEqual(hash.hex, expect)
        }
    }
    
    func test256StaticBytesApi() {
        for (input, expect) in testCases256 {
            let hash = SHA2.hash(type: .sha256, bytes: Array(input))
            XCTAssertEqual(hash.hex, expect)
        }
    }
    
    func test512StreamingDataApi() {
        var sha2 = SHA2(type: .sha512)
        for (input, expect) in testCases512 {
            sha2.update(input)
            XCTAssertEqual(sha2.finalize().hex, expect)
            sha2.reset()
        }
    }
    
    func test512StreamingBytesApi() {
        var sha2 = SHA2(type: .sha512)
        for (input, expect) in testCases512 {
            sha2.update(Array(input))
            XCTAssertEqual(sha2.finalize().hex, expect)
            sha2.reset()
        }
    }
    
    func test512StaticDataApi() {
        for (input, expect) in testCases512 {
            let hash = SHA2.hash(type: .sha512, data: input)
            XCTAssertEqual(hash.hex, expect)
        }
    }
    
    func test512StaticBytesApi() {
        for (input, expect) in testCases512 {
            let hash = SHA2.hash(type: .sha512, bytes: Array(input))
            XCTAssertEqual(hash.hex, expect)
        }
    }
}
