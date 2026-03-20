import Foundation

enum CoinFormatter {

    static let microPerUnit: Decimal = 1_000_000

    static func microToDisplay(_ micro: String) -> String {
        guard let value = Decimal(string: micro) else { return "0" }
        let display = value / microPerUnit
        return "\(display)"
    }

    static func displayToMicro(_ display: String) -> String {
        guard let value = Decimal(string: display) else { return "0" }
        let micro = value * microPerUnit
        return "\(micro)"
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
