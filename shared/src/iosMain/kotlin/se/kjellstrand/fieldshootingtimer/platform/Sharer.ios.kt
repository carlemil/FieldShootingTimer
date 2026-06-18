package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

class IosSharer : Sharer {
    override fun share(text: String) {
        val controller = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )

        // Walk to the topmost presented view controller so the sheet shows
        // even when something is already presented.
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (top?.presentedViewController != null) {
            top = top.presentedViewController
        }

        // Required on iPad (and harmless on iPhone) so the popover has an anchor.
        controller.popoverPresentationController?.sourceView = top?.view

        top?.presentViewController(controller, animated = true, completion = null)
    }
}

@Composable
actual fun rememberSharer(): Sharer = remember { IosSharer() }
