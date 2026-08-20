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
    val color: Color,
    // Whether this command's segment is drawn on the dial. Everything after
    // CeaseFire still runs on the timer (audio cues, list highlight) but is
    // not shown as dial segments — the hand parks at the dial's end.
    val onDial: Boolean = false,
    // Silent gap entries pace the sequence but get no command-list row;
    // their stringRes is a never-shown placeholder.
    val listed: Boolean = true
) {
    // Untimed but audible: called during the competition preparation
    // countdown (Load at its start, AllReady at -10s) via
    // buildCompetitionPrepCues.
    Load("files/ladda.mp3", Res.string.command_load, -1, LightGrayColor),
    AllReady("files/alla_klara.mp3", Res.string.command_all_ready, -1, LightGrayColor),
    TenSecondsLeft("files/tio_sekunder_kvar.mp3", Res.string.command_10_seconds, 7, LightGrayColor, onDial = true),
    Ready("files/fardiga.mp3", Res.string.command_ready, 3, LightGrayColor, onDial = true),
    Fire("files/eld.mp3", Res.string.command_fire, 0, LightGreenColor, onDial = true),
    CeaseFire("files/eld_upp_hor.mp3", Res.string.command_cease_fire, 3, MutedYellowColor, onDial = true),
    // The beat after the cease-fire moment before "Patron ur!" is called.
    UnloadWeaponDelay(null, Res.string.command_unload_weapon, 3, LightGrayColor, listed = false),
    UnloadWeapon("files/patron_ur_proppa_vapen.mp3", Res.string.command_unload_weapon, 4, RedColor),
    // The beat before "Visitation!" is called.
    VisitationDelay(null, Res.string.command_inspection, 2, LightGrayColor, listed = false),
    Visitation("files/visitation.mp3", Res.string.command_inspection, 2, LightGrayColor),
    // Untimed, dialog-driven calls: VisitationDone asks after the sequence
    // finishes (or when its row is tapped) and hands over to Mark's dialog.
    VisitationDone("files/visitation_klar.mp3", Res.string.command_visitation_done, -1, LightGrayColor),
    Mark("files/markera.mp3", Res.string.command_mark, -1, LightGrayColor);

    companion object {
        val timedCommands: List<Command> = entries.filter { it.duration >= 0 }
        val audibleCommands: List<Command> = entries.filter { it.audioPath != null }

        /** The commands shown as rows in the command list. */
        val listedCommands: List<Command> = entries.filter { it.listed }

        /**
         * The prefix of [timedCommands] drawn as dial segments (through
         * CeaseFire); the timer keeps running past them.
         */
        val dialCommands: List<Command> = timedCommands.filter { it.onDial }

        /** Index of the Fire segment in [timedCommands]-ordered lists. */
        val fireSegmentIndex: Int = timedCommands.indexOf(Fire)
    }
}
