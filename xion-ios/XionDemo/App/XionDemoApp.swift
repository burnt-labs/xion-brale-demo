import SwiftUI

@main
struct XionDemoApp: App {
    @StateObject private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            AppNavigation(container: container)
                .preferredColorScheme(.dark)
        }
    }
}
