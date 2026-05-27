import SwiftUI
import AVFoundation

@main
struct iosAppApp: App {
    init() {
        // Configure AVAudioSession `.ambient` so playback respects the silent
        // switch automatically. Matches Android's RINGER_MODE_SILENT behavior
        // for audio (haptics fire unconditionally, like Apple's Timer app).
        do {
            try AVAudioSession.sharedInstance().setCategory(.ambient)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Failed to configure AVAudioSession: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView().ignoresSafeArea()
        }
    }
}
