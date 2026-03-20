import SwiftUI

struct LoadingOverlay: View {
    let isVisible: Bool
    let message: String

    var body: some View {
        if isVisible {
            ZStack {
                Color.black.opacity(0.5)
                    .ignoresSafeArea()

                VStack(spacing: 16) {
                    ProgressView()
                        .scaleEffect(1.5)
                        .tint(.white)

                    Text(message)
                        .font(.xionBodySmall)
                        .foregroundStyle(.white)
                        .multilineTextAlignment(.center)
                }
                .padding(32)
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .transition(.opacity)
        }
    }
}
