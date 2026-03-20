// swift-tools-version:5.4
// The swift-tools-version declares the minimum version of Swift required to build this package.
import PackageDescription

let package = Package(
    name: "UncommonCrypto",
    platforms: [.macOS(.v10_13), .iOS(.v11), .tvOS(.v11), .watchOS(.v6)],
    products: [
        .library(
            name: "UncommonCrypto",
            targets: ["UncommonCrypto"]),
        .library(
            name: "CUncommonCrypto",
            targets: ["CUncommonCrypto"])
    ],
    dependencies: [],
    targets: [
        .target(
            name: "UncommonCrypto",
            dependencies: ["CUncommonCrypto"]),
        .target(
            name: "CUncommonCrypto",
            dependencies: []
        ),
        .testTarget(
            name: "UncommonCryptoTests",
            dependencies: ["UncommonCrypto"])
    ]
)
