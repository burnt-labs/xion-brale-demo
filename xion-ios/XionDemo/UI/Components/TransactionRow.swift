import SwiftUI

struct CompactTransactionRow: View {
    let transaction: TransactionResult

    var body: some View {
        VStack(spacing: 0) {
            // Row 1: Status icon + tx hash + type badge
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: iconName)
                        .foregroundStyle(iconColor)
                        .font(.system(size: 18))

                    Text(shortHash)
                        .font(.system(size: 14, weight: .medium, design: .monospaced))
                        .foregroundStyle(Color.greetingText)
                }

                Spacer()

                HStack(spacing: 6) {
                    if !transaction.status.isEmpty {
                        Text(transaction.status)
                            .font(.system(size: 11, weight: .medium))
                            .foregroundStyle(statusColor)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(statusColor.opacity(0.12))
                            .clipShape(RoundedRectangle(cornerRadius: 4))
                    }

                    if !transaction.txType.isEmpty {
                        Text(transaction.txType)
                            .font(.system(size: 11))
                            .foregroundStyle(Color.subtitleText)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .overlay(
                                RoundedRectangle(cornerRadius: 4)
                                    .stroke(Color.subtitleText.opacity(0.4), lineWidth: 1)
                            )
                    }
                }
            }

            Spacer().frame(height: 10)

            // Detail rows
            if !transaction.displayAmount.isEmpty {
                TxDetailRow(label: "Amount", value: transaction.displayAmount)
            } else if !transaction.amount.isEmpty {
                TxDetailRow(label: "Amount", value: CoinFormatter.formatWithDenom(transaction.amount, denom: amountDisplayDenom))
            }
            if !transaction.recipient.isEmpty {
                TxDetailRow(label: "To", value: shortRecipient)
            }
            if !transaction.fee.isEmpty {
                TxDetailRow(label: "Tx fee", value: CoinFormatter.formatWithDenom(transaction.fee))
            }
            if transaction.height > 0 {
                TxDetailRow(label: "Height", value: formattedHeight)
            }
            if !formattedTime.isEmpty {
                TxDetailRow(label: "Time", value: formattedTime)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
    }

    /// Determine the display denom based on amountDenom field or Brale transaction type
    private var amountDisplayDenom: String {
        // If the transaction carries an explicit amountDenom, use it
        if !transaction.amountDenom.isEmpty {
            if transaction.amountDenom.lowercased().contains("sbc") {
                return Constants.sbcDisplayDenom
            }
            return Constants.displayDenom
        }
        // Detect Buy SBC / Cash Out by txType or Brale custodial address prefix
        let txType = transaction.txType.lowercased()
        if txType.contains("buy sbc") || txType.contains("cash out") {
            return Constants.sbcDisplayDenom
        }
        if transaction.recipient.hasPrefix("xion1amma") {
            return Constants.sbcDisplayDenom
        }
        return Constants.displayDenom
    }

    private var iconName: String {
        if transaction.inProgress { return "clock.fill" }
        return transaction.success ? "checkmark.circle.fill" : "xmark.circle.fill"
    }

    private var iconColor: Color {
        if transaction.inProgress { return .orange }
        return transaction.success ? Color(red: 0, green: 0.9, blue: 0.46) : .red
    }

    private var statusColor: Color {
        switch transaction.status {
        case "Completed": return Color(red: 0, green: 0.7, blue: 0.4)
        case "Failed", "Canceled": return .red
        default: return .orange // Pending / Processing
        }
    }

    private var shortHash: String {
        guard transaction.txHash.count > 12 else { return transaction.txHash }
        return "\(transaction.txHash.prefix(6))...\(transaction.txHash.suffix(6))"
    }

    private var shortRecipient: String {
        guard transaction.recipient.count > 12 else { return transaction.recipient }
        return "\(transaction.recipient.prefix(8))...\(transaction.recipient.suffix(4))"
    }

    private var formattedHeight: String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.locale = Locale(identifier: "en_US")
        return formatter.string(from: NSNumber(value: transaction.height)) ?? "\(transaction.height)"
    }

    private var formattedTime: String {
        formatTxTimestamp(transaction.timestamp)
    }
}

private struct TxDetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(.system(size: 13))
                .foregroundStyle(Color.subtitleText)
            Spacer()
            Text(value)
                .font(.system(size: 13))
                .foregroundStyle(Color.greetingText)
        }
        .padding(.vertical, 3)
    }
}

private func formatTxTimestamp(_ isoTimestamp: String) -> String {
    guard !isoTimestamp.isEmpty else { return "" }

    let parser = DateFormatter()
    parser.dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    parser.timeZone = TimeZone(identifier: "UTC")
    parser.locale = Locale(identifier: "en_US_POSIX")

    guard let date = parser.date(from: isoTimestamp) else { return isoTimestamp }

    let display = DateFormatter()
    display.dateFormat = "MMM d, yyyy, HH:mm:ss"
    display.locale = Locale(identifier: "en_US")
    let formatted = display.string(from: date)

    let diffSeconds = Int(Date().timeIntervalSince(date))
    let relative: String
    switch diffSeconds {
    case ..<60:
        relative = "just now"
    case ..<3600:
        let mins = diffSeconds / 60
        relative = "\(mins) min ago"
    case ..<86400:
        let hours = diffSeconds / 3600
        relative = "\(hours) hours ago"
    default:
        let days = diffSeconds / 86400
        relative = "\(days) days ago"
    }

    return "\(formatted) (\(relative))"
}
