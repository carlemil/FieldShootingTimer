package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.graphics.Color
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.MutedYellowColor
import se.kjellstrand.fieldshootingtimer.ui.theme.RedColor

// TODO: audioResId / stringResId placeholders are wired to real
// Compose Multiplatform resources in task shared/compose-resources.
enum class Command(val audioResId: Int, val stringResId: Int, val duration: Int, val color: Color) {
    Load(-1, 0, -1, LightGrayColor),
    AllReady(-1, 0, -1, LightGrayColor),
    TenSecondsLeft(0, 0, 7, LightGrayColor),
    Ready(0, 0, 3, LightGrayColor),
    Fire(0, 0, 0, LightGreenColor),
    CeaseFire(0, 0, 3, MutedYellowColor),
    UnloadWeapon(0, 0, 4, RedColor),
    Visitation(0, 0, 2, LightGrayColor),
    Mark(-1, 0, -1, LightGrayColor);

    companion object {
        val timedCommands: List<Command> = entries.filter { it.duration >= 0 }
        val audibleCommands: List<Command> = entries.filter { it.audioResId != -1 }
    }
}
