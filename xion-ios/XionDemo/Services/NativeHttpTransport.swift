import Foundation

/// HttpTransport implementation using URLSession for synchronous HTTP POST.
/// The Rust FFI calls this from a background thread, so it must be synchronous.
final class NativeHttpTransport: HttpTransport {

    func post(url: String, body: Data) throws -> Data {
        guard let requestUrl = URL(string: url) else {
            throw TransportError.RequestFailed(message: "Invalid URL: \(url)")
        }

        var request = URLRequest(url: requestUrl)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = body

        var responseData: Data?
        var responseError: Error?

        let semaphore = DispatchSemaphore(value: 0)

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            defer { semaphore.signal() }

            if let error = error {
                responseError = error
                return
            }

            guard let httpResponse = response as? HTTPURLResponse else {
                responseError = TransportError.RequestFailed(message: "No HTTP response")
                return
            }

            guard let data = data else {
                responseError = TransportError.RequestFailed(message: "No response data")
                return
            }

            if httpResponse.statusCode < 200 || httpResponse.statusCode >= 300 {
                let body = String(data: data, encoding: .utf8) ?? ""
                responseError = TransportError.RequestFailed(
                    message: "HTTP \(httpResponse.statusCode): \(body)"
                )
                return
            }

            responseData = data
        }

        task.resume()
        semaphore.wait()

        if let error = responseError {
            throw error
        }

        guard let data = responseData else {
            throw TransportError.RequestFailed(message: "No response data")
        }

        return data
    }
}
