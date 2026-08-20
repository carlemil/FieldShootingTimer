package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// The jvm target exists only for host-side tests; no mail client is opened.
private class NoOpFeedbackSender : FeedbackSender {
    override fun sendFeedback() = Unit
}

@Composable
actual fun rememberFeedbackSender(): FeedbackSender = remember { NoOpFeedbackSender() }
