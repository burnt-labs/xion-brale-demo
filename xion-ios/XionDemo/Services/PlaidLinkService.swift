import Foundation
import LinkKit
import UIKit

enum PlaidLinkResult {
    case success(publicToken: String)
    case cancelled
}

enum PlaidLinkError: LocalizedError {
    case handlerCreationFailed(Error)
    case noPresenter

    var errorDescription: String? {
        switch self {
        case .handlerCreationFailed(let error):
            return "Failed to create Plaid Link: \(error.localizedDescription)"
        case .noPresenter:
            return "Unable to present Plaid Link"
        }
    }
}

final class PlaidLinkService {

    private var handler: Handler?

    @MainActor
    func openLink(token: String) async throws -> PlaidLinkResult {
        return try await withCheckedThrowingContinuation { [weak self] continuation in
            var configuration = LinkTokenConfiguration(token: token) { success in
                self?.handler = nil
                continuation.resume(returning: .success(publicToken: success.publicToken))
            }
            configuration.onExit = { [weak self] exit in
                self?.handler = nil
                if let error = exit.error {
                    continuation.resume(throwing: PlaidLinkError.handlerCreationFailed(
                        NSError(domain: "PlaidLink", code: Int(error.errorCode.rawValue),
                                userInfo: [NSLocalizedDescriptionKey: error.errorMessage])
                    ))
                } else {
                    continuation.resume(returning: .cancelled)
                }
            }

            let result = Plaid.create(configuration)
            switch result {
            case .success(let handler):
                self?.handler = handler
                guard let viewController = Self.topViewController() else {
                    self?.handler = nil
                    continuation.resume(throwing: PlaidLinkError.noPresenter)
                    return
                }
                handler.open(presentUsing: .viewController(viewController))
            case .failure(let error):
                continuation.resume(throwing: PlaidLinkError.handlerCreationFailed(error))
            }
        }
    }

    private static func topViewController() -> UIViewController? {
        guard let scene = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .first(where: { $0.activationState == .foregroundActive }),
              let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController else {
            return nil
        }
        var top = root
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }
}
