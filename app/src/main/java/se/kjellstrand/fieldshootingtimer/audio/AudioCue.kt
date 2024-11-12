package se.kjellstrand.fieldshootingtimer.audio

import se.kjellstrand.fieldshootingtimer.ui.Command

data class AudioCue(
    val time: Float,
    val command: Command
)