// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "Mob",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "Mob",
            targets: ["Mob"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "mobFFI",
            path: "lib/libmob.xcframework"
        ),
        .target(
            name: "Mob",
            dependencies: ["mobFFI"],
            path: "Sources/Mob",
            sources: ["mob.swift", "NativeHttpTransport.swift", "Compat.swift"]
        ),
    ]
)
