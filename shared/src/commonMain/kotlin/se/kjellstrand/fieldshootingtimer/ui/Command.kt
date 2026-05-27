package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.resources.command_10_seconds
import se.kjellstrand.fieldshootingtimer.resources.command_all_ready
import se.kjellstrand.fieldshootingtimer.resources.command_cease_fire
import se.kjellstrand.fieldshootingtimer.resources.command_fire
import se.kjellstrand.fieldshootingtimer.resources.command_inspection
import se.kjellstrand.fieldshootingtimer.resources.command_load
import se.kjellstrand.fieldshootingtimer.resources.command_mark
import se.kjellstrand.fieldshootingtimer.resources.command_ready
import se.kjellstrand.fieldshootingtimer.resources.command_unload_weapon
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.MutedYellowColor
import se.kjellstrand.fieldshootingtimer.ui.theme.RedColor

enum class Command(
    val audioPath: String?,
    val stringRes: StringResource,
    val duration: Int,
    val color: Color
) {
    Load(null, Res.string.command_load, -1, LightGrayColor),
    AllReady(null, Res.string.command_all_ready, -1, LightGrayColor),
    TenSecondsLeft("files/tio_sekunder_kvar.mp3", Res.string.command_10_seconds, 7, LightGrayColor),
    Ready("files/fardiga.mp3", Res.string.command_ready, 3, LightGrayColor),
    Fire("files/eld.mp3", Res.string.command_fire, 0, LightGreenColor),
    CeaseFire("files/eld_upp_hor.mp3", Res.string.command_cease_fire, 3, MutedYellowColor),
    UnloadWeapon("files/patron_ur_proppa_vapen.mp3", Res.string.command_unload_weapon, 4, RedColor),
    Visitation("files/visitation.mp3", Res.string.command_inspection, 2, LightGrayColor),
    Mark(null, Res.string.command_mark, -1, LightGrayColor);

    companion object {
        val timedCommands: List<Command> = entries.filter { it.duration >= 0 }
        val audibleCommands: List<Command> = entries.filter { it.audioPath != null }
    }
}
