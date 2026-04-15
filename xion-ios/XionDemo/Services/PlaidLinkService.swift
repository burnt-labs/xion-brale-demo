import Foundation
import LinkKit
import UIKit

struct PlaidLinkSuccessInfo {
    let publicToken: String
    let linkSessionId: String
}

struct PlaidLinkExitInfo {
    let linkSessionId: String?
    let requestId: String?
    let errorMessage: String?
    let errorCode: String?
}

enum PlaidLinkResult {
    case success(PlaidLinkSuccessInfo)
    case cancelled(PlaidLinkExitInfo)
}

enum PlaidLinkError: LocalizedError {
    case handlerCreationFailed(Error)
    case noPresenter
    case linkKitError(PlaidLinkExitInfo)

    var errorDescription: String? {
        switch self {
        case .handlerCreationFailed(let error):
            return "Failed to create Plaid Link: \(error.localizedDescription)"
        case .noPresenter:
            return "Unable to present Plaid Link"
        case .linkKitError(let info):
            return info.errorMessage ?? "Plaid Link error"
        }
    }
}

final class PlaidLinkService {

    private var handler: Handler?

    @MainActor
    func openLink(token: String) async throws -> PlaidLinkResult {
        return try await withCheckedThrowingContinuation { [weak self] continuation in
            var configuration = LinkTokenConfiguration(token: token) { [weak self] success in
                self?.handler = nil
                let info = PlaidLinkSuccessInfo(
                    publicToken: success.publicToken,
                    linkSessionId: success.metadata.linkSessionID
                )
                print("[PlaidLink] success linkSessionID=\(info.linkSessionId)")
                continuation.resume(returning: .success(info))
            }
            configuration.onExit = { [weak self] exit in
                self?.handler = nil
                let info = PlaidLinkExitInfo(
                    linkSessionId: exit.metadata.linkSessionID,
                    requestId: exit.metadata.requestID,
                    errorMessage: exit.error?.errorMessage,
                    errorCode: exit.error.map { "\($0.errorCode)" }
                )
                print("[PlaidLink] exit linkSessionID=\(info.linkSessionId ?? "nil") requestID=\(info.requestId ?? "nil") error=\(info.errorMessage ?? "nil")")
                if exit.error != nil {
                    continuation.resume(throwing: PlaidLinkError.linkKitError(info))
                } else {
                    continuation.resume(returning: .cancelled(info))
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

    /// Forward a Plaid OAuth Universal Link back into the active Plaid Link
    /// handler so the hand-off from Safari completes. Called from
    /// `.onContinueUserActivity(NSUserActivityTypeBrowsingWeb)` when the
    /// bank redirects to our iOS redirect URI.
    @MainActor
    func continueFromRedirectUri(_ url: URL) {
        handler?.resumeAfterTermination(from: url)
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
