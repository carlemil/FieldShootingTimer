package se.kjellstrand.fieldshootingtimer

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import se.kjellstrand.fieldshootingtimer.ui.theme.FieldShootingTimerTheme

/**
 * Bridge entry point for the iOS Xcode app. Wrap this UIViewController in a
 * UIViewControllerRepresentable to embed the Compose UI in SwiftUI, or push
 * directly from UIKit.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    FieldShootingTimerTheme {
        MainScreen()
    }
}
