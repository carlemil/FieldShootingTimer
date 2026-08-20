package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable

/** Where the menu's feedback item sends its mail draft. */
internal const val FEEDBACK_EMAIL = "erbsman@gmail.com"

/**
 * Opens a pre-filled feedback e-mail draft in the platform's default mail
 * client: recipient [FEEDBACK_EMAIL], a platform-tagged subject
 * ("Ang. FältSkytteTimer appen på Android/iOS"), and a body that ends with
 * a debug block (app version, OS version, device model, locale) so the
 * recipient can understand the sender's environment.
 */
interface FeedbackSender {
    fun sendFeedback()
}

@Composable
expect fun rememberFeedbackSender(): FeedbackSender
