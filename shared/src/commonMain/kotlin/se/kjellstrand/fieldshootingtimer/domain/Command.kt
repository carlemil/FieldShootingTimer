package se.kjellstrand.fieldshootingtimer.domain

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
import se.kjellstrand.fieldshootingtimer.resources.command_visitation_done
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
    // Untimed, dialog-driven calls that open a competition run: play (or
    // the row) asks "Ladda?", confirming asks "Alla klara?", confirming
    // that starts the timed sequence.
    Load("files/ladda.mp3", Res.string.command_load, -1, LightGrayColor),
    AllReady("files/alla_klara.mp3", Res.string.command_all_ready, -1, LightGrayColor),
    TenSecondsLeft("files/tio_sekunder_kvar.mp3", Res.string.command_10_seconds, 7, LightGrayColor),
    Ready("files/fardiga.mp3", Res.string.command_ready, 3, LightGrayColor),
    Fire("files/eld.mp3", Res.string.command_fire, 0, LightGreenColor),
    CeaseFire("files/eld_upp_hor.mp3", Res.string.command_cease_fire, 3, MutedYellowColor),
    // Untimed, dialog-driven calls that close the run: the finished timer
    // asks "Patron ur?", then (competition only) "Visitation?", then
    // "Visitation klar?", each confirmation making the call and handing
    // over to the next; Mark's dialog is the last. Tapping a row asks the
    // same question.
    UnloadWeapon("files/patron_ur_proppa_vapen.mp3", Res.string.command_unload_weapon, -1, RedColor),
    Visitation("files/visitation.mp3", Res.string.command_inspection, -1, LightGrayColor),
    VisitationDone("files/visitation_klar.mp3", Res.string.command_visitation_done, -1, LightGrayColor),
    Mark("files/markera.mp3", Res.string.command_mark, -1, LightGrayColor);

    companion object {
        val timedCommands: List<Command> = entries.filter { it.duration >= 0 }
        val audibleCommands: List<Command> = entries.filter { it.audioPath != null }

        /** Index of the Fire segment in [timedCommands]-ordered lists. */
        val fireSegmentIndex: Int = timedCommands.indexOf(Fire)
    }
}
