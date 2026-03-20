import SwiftUI

struct TransactionRow: View {
    let transaction: TransactionResult

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: transaction.success ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundStyle(transaction.success ? .green : .red)
                .font(.title3)

            VStack(alignment: .leading, spacing: 4) {
                Text(truncatedHash(transaction.txHash))
                    .font(.xionTitleMedium)
                    .lineLimit(1)

                HStack(spacing: 8) {
                    Text("Height: \(transaction.height)")
                        .font(.xionLabelSmall)
                        .foregroundStyle(.secondary)

                    Text("Gas: \(transaction.gasUsed)")
                        .font(.xionLabelSmall)
                        .foregroundStyle(.secondary)
                }
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundStyle(.tertiary)
        }
        .padding(16)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private func truncatedHash(_ hash: String) -> String {
        guard hash.count > 20 else { return hash }
        let prefix = hash.prefix(12)
        let suffix = hash.suffix(8)
        return "\(prefix)...\(suffix)"
    }
}
