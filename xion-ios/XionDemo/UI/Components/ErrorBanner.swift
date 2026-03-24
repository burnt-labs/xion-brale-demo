import SwiftUI

struct ErrorBanner: View {
    let message: String?
    let onDismiss: () -> Void
    var onRetry: (() -> Void)? = nil

    var body: some View {
        if let message = message {
            HStack(spacing: 12) {
                Image(systemName: "exclamationmark.circle.fill")
                    .foregroundStyle(.red)
                    .font(.system(size: 20))

                Text(message)
                    .font(.system(size: 14))
                    .foregroundStyle(.primary)
                    .lineLimit(3)

                Spacer()

                if let onRetry = onRetry {
                    Button(action: onRetry) {
                        Text("Retry")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(.red)
                    }
                }

                Button(action: onDismiss) {
                    Text("Dismiss")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(.red)
                }
            }
            .padding(16)
            .background(Color.red.opacity(0.1))
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }
}
