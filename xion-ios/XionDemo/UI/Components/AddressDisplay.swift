import SwiftUI

struct AddressDisplay: View {
    let address: String

    @State private var copied = false

    var body: some View {
        Button(action: copyAddress) {
            HStack(spacing: 8) {
                Text(truncatedAddress)
                    .font(.system(.body, design: .monospaced))
                    .foregroundStyle(.primary)

                Image(systemName: copied ? "checkmark" : "doc.on.doc")
                    .font(.caption)
                    .foregroundStyle(copied ? .green : .secondary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(.ultraThinMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }

    private var truncatedAddress: String {
        guard address.count > 20 else { return address }
        let prefix = address.prefix(12)
        let suffix = address.suffix(8)
        return "\(prefix)...\(suffix)"
    }

    private func copyAddress() {
        UIPasteboard.general.string = address
        copied = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
            copied = false
        }
    }
}
