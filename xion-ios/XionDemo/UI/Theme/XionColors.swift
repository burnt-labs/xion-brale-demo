import SwiftUI

extension Color {
    static let xionGreen = Color(red: 0.0, green: 0.85, blue: 0.65)
    static let xionGreenDark = Color(red: 0.0, green: 0.65, blue: 0.50)
    static let xionBlue = Color(red: 0.25, green: 0.45, blue: 0.85)
    static let xionRed = Color(red: 0.90, green: 0.25, blue: 0.30)
    static let xionBackground = Color(red: 0.05, green: 0.05, blue: 0.08)
    static let xionSurface = Color(red: 0.10, green: 0.10, blue: 0.14)
    static let xionSurfaceVariant = Color(red: 0.15, green: 0.15, blue: 0.20)
    static let xionOnSurface = Color(red: 0.92, green: 0.92, blue: 0.95)
    static let xionOnSurfaceVariant = Color(red: 0.65, green: 0.65, blue: 0.70)

    // Card-based UI colors (Step 6 redesign)
    static let screenBackground = Color(UIColor.systemGroupedBackground)
    static let cardBackground = Color(UIColor.systemBackground)
    static let cardShadow = Color.black.opacity(0.03)
    static let greetingText = Color(red: 0.17, green: 0.17, blue: 0.18)
    static let subtitleText = Color(red: 0.56, green: 0.56, blue: 0.58)
    static let xionOrange = Color(red: 1.0, green: 0.6, blue: 0.0)
}
