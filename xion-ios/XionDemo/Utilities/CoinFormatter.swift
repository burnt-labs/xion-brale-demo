import Foundation

enum CoinFormatter {

    static let microPerUnit: Decimal = 1_000_000

    private static let displayFormatter: NumberFormatter = {
        let f = NumberFormatter()
        f.numberStyle = .decimal
        f.maximumFractionDigits = 6
        f.minimumFractionDigits = 0
        f.locale = Locale(identifier: "en_US")
        return f
    }()

    static func microToDisplay(_ micro: String) -> String {
        guard let value = Decimal(string: micro) else { return "0" }
        let display = value / microPerUnit
        return displayFormatter.string(from: display as NSDecimalNumber) ?? "\(display)"
    }

    static func displayToMicro(_ display: String) -> String {
        guard let value = Decimal(string: display) else { return "0" }
        var micro = value * microPerUnit
        // Round down to nearest whole number
        var rounded = Decimal()
        NSDecimalRound(&rounded, &micro, 0, .down)
        return "\(rounded)"
    }

    static func formatBalance(_ micro: String, denom: String = Constants.displayDenom) -> String {
        let display = microToDisplay(micro)
        return "\(display) \(denom)"
    }

    static func formatWithDenom(_ micro: String, denom: String = Constants.displayDenom) -> String {
        let display = microToDisplay(micro)
        return "\(display) \(denom)"
    }

    static func isValidAmount(_ amount: String) -> Bool {
        guard let value = Decimal(string: amount) else { return false }
        return value > 0
    }
}
