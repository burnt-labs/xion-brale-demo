import SwiftUI

@main
struct XionDemoApp: App {
    @StateObject private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            AppNavigation(container: container)
                .preferredColorScheme(.light)
                .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
                    guard let url = activity.webpageURL else { return }
                    container.plaidLinkService.continueFromRedirectUri(url)
                }
        }
    }
}
