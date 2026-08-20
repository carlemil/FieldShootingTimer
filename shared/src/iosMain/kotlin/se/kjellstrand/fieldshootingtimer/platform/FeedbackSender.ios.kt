package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.create
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

@OptIn(kotlinx.cinterop.BetaInteropApi::class)
private fun String.urlEncoded(): String =
    NSString.create(string = this)
        .stringByAddingPercentEncodingWithAllowedCharacters(
            NSCharacterSet.URLQueryAllowedCharacterSet
        ) ?: this

private class IosFeedbackSender : FeedbackSender {

    override fun sendFeedback() {
        val bundle = NSBundle.mainBundle
        val version = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "?"
        val build = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String ?: "?"
        val device = UIDevice.currentDevice
        val body = """


----
Debug:
App: $version ($build)
OS: ${device.systemName} ${device.systemVersion}
Enhet: ${device.model}
Språk: ${NSLocale.currentLocale.localeIdentifier}
"""
        val subject = "Ang. FältSkytteTimer appen på iOS"
        val url = NSURL.URLWithString(
            "mailto:$FEEDBACK_EMAIL?subject=${subject.urlEncoded()}&body=${body.urlEncoded()}"
        ) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

@Composable
actual fun rememberFeedbackSender(): FeedbackSender = remember { IosFeedbackSender() }
