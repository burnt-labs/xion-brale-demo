import SwiftUI

struct BalanceCard: View {
    let balance: String?
    let denom: String
    let isLoading: Bool

    var body: some View {
        VStack(spacing: 8) {
            Text("Balance")
                .font(.xionLabelSmall)
                .foregroundStyle(.secondary)

            if isLoading {
                ProgressView()
                    .frame(height: 32)
            } else {
                Text(balance.map { CoinFormatter.formatBalance($0) } ?? "--")
                    .font(.xionHeadlineLarge)
                    .foregroundStyle(.primary)
            }

            Text(denom)
                .font(.xionLabelSmall)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}
