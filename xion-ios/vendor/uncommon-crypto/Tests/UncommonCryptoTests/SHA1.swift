//
//  SHA1.swift
//
//
//  Created by Yehor Popovych on 20.05.2021.
//

import XCTest
@testable import UncommonCrypto

final class SHA1Tests: XCTestCase {
    let testCases: [(input: Data, hash: String)] = [
        ("test1".utf8, "b444ac06613fc8d63795be9ad0beaf55011936ac"),
        (Data(), "da39a3ee5e6b4b0d3255bfef95601890afd80709"),
        ("Some data for hashing".utf8, "f82503d847bc6616b5147d27cd8fc9f7b73ead31")
    ]
    
    func testStreamingDataApi() {
        var sha1 = SHA1()
        for (input, expect) in testCases {
            sha1.update(input)
            XCTAssertEqual(sha1.finalize().hex, expect)
            sha1.reset()
        }
    }
    
    func testStreamingBytesApi() {
        var sha1 = SHA1()
        for (input, expect) in testCases {
            sha1.update(Array(input))
            XCTAssertEqual(sha1.finalize().hex, expect)
            sha1.reset()
        }
    }
    
    func testStaticDataApi() {
        for (input, expect) in testCases {
            let hash = SHA1.hash(data: input)
            XCTAssertEqual(hash.hex, expect)
        }
    }
    
    func testStaticBytesApi() {
        for (input, expect) in testCases {
            let hash = SHA1.hash(bytes: Array(input))
            XCTAssertEqual(hash.hex, expect)
        }
    }
}
