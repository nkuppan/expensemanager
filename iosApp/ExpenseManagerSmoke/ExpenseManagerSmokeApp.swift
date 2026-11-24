import SwiftUI

/// Throwaway smoke app: proves the shared Kotlin framework links and runs on iOS
/// (OpenSpec change add-ios-smoke-app). Phase 3 replaces it with the real app.
@main
struct ExpenseManagerSmokeApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
