package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.graphics.Color
import se.kjellstrand.fieldshootingtimer.R
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGrayColor
import se.kjellstrand.fieldshootingtimer.ui.theme.LightGreenColor
import se.kjellstrand.fieldshootingtimer.ui.theme.MutedYellowColor
import se.kjellstrand.fieldshootingtimer.ui.theme.RedColor

enum class Command(val audioResId: Int, val stringResId: Int, val duration: Int, val color: Color) {
    Load(-1, R.string.command_load, -1, LightGrayColor),
    AllReady(-1, R.string.command_all_ready, -1, LightGrayColor),
    TenSecondsLeft(R.raw.tio_sekunder_kvar, R.string.command_10_seconds, 7, LightGrayColor),
    Ready(R.raw.fardiga, R.string.command_ready, 3, LightGrayColor),
    Fire(R.raw.eld, R.string.command_fire, 0, LightGreenColor),
    CeaseFire(R.raw.eld_upp_hor, R.string.command_cease_fire, 3, MutedYellowColor),
    UnloadWeapon(R.raw.patron_ur_proppa_vapen, R.string.command_unload_weapon, 4, RedColor),
    Visitation(R.raw.visitation, R.string.command_inspection, 2, LightGrayColor),
    Mark(-1, R.string.command_mark, -1, LightGrayColor);
}
