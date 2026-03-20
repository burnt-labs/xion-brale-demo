import Combine
import Foundation

@MainActor
final class ConnectViewModel: ObservableObject {

    @Published var isLoading = false
    @Published var loadingMessage = "Connecting..."
    @Published var error: String?
    @Published var isConnected = false

    private let repository: XionRepositoryProtocol
    private var cancellables = Set<AnyCancellable>()

    init(repository: XionRepositoryProtocol) {
        self.repository = repository

        repository.sessionManager.$walletState
            .receive(on: DispatchQueue.main)
            .sink { [weak self] state in
                self?.handleStateChange(state)
            }
            .store(in: &cancellables)

        Task {
            let restored = await repository.restoreSession()
            if restored {
                isConnected = true
            }
        }
    }

    func startOAuthFlow() {
        Task {
            isLoading = true
            error = nil
            loadingMessage = "Authenticating..."

            do {
                _ = try await repository.connect()
                isConnected = true
            } catch {
                self.error = error.localizedDescription
            }

            isLoading = false
        }
    }

    func clearError() {
        error = nil
    }

    private func handleStateChange(_ state: WalletState) {
        switch state {
        case .connected:
            isConnected = true
            isLoading = false
        case .connecting(let step):
            isLoading = true
            switch step {
            case .authenticating:
                loadingMessage = "Authenticating..."
            case .generatingSessionKey:
                loadingMessage = "Generating session key..."
            case .settingUpGrants:
                loadingMessage = "Setting up authorization grants..."
            case .verifyingGrants:
                loadingMessage = "Verifying grants..."
            }
        case .disconnected:
            isConnected = false
            isLoading = false
        }
    }
}
