import SwiftUI

enum Route: Hashable {
    case wallet
    case contract
    case history
    case onramp
    case offramp
}

struct AppNavigation: View {
    @ObservedObject var container: AppContainer

    @State private var isConnected = false
    @State private var path = NavigationPath()

    var body: some View {
        if isConnected {
            NavigationStack(path: $path) {
                WalletView(
                    viewModel: WalletViewModel(repository: container.repository),
                    sendViewModel: SendViewModel(repository: container.repository),
                    onNavigateToContract: { path.append(Route.contract) },
                    onNavigateToHistory: { path.append(Route.history) },
                    onNavigateToOnramp: { path.append(Route.onramp) },
                    onNavigateToOfframp: { path.append(Route.offramp) },
                    onDisconnected: { isConnected = false }
                )
                .navigationDestination(for: Route.self) { route in
                    switch route {
                    case .contract:
                        ContractView(viewModel: ContractViewModel(repository: container.repository))
                    case .history:
                        HistoryView(viewModel: HistoryViewModel(repository: container.repository))
                    case .onramp:
                        OnrampView(
                            viewModel: OnrampViewModel(
                                braleRepository: container.braleRepository,
                                xionRepository: container.repository,
                                secureStorage: container.secureStorage,
                                plaidLinkService: container.plaidLinkService
                            ),
                            onDone: { path.removeLast() }
                        )
                    case .offramp:
                        OfframpView(
                            viewModel: OfframpViewModel(
                                braleRepository: container.braleRepository,
                                xionRepository: container.repository,
                                secureStorage: container.secureStorage
                            ),
                            onDone: { path.removeLast() }
                        )
                    case .wallet:
                        EmptyView()
                    }
                }
            }
        } else {
            ConnectView(
                viewModel: ConnectViewModel(repository: container.repository),
                onConnected: { isConnected = true }
            )
        }
    }
}
