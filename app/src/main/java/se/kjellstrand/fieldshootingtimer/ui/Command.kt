package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.R

enum class Command(val audioResId: Int, val stringResId: Int, val duration: Int) {
    Load(-1, R.string.command_load, -1),
    AllReady(-1, R.string.command_all_ready, -1),
    TenSecondsLeft(R.raw.tio_sekunder_kvar, R.string.command_10_seconds, 7),
    Ready(R.raw.fardiga, R.string.command_ready, 3),
    Fire(R.raw.eld, R.string.command_fire, 0),
    CeaseFire(R.raw.eld_upp_hor, R.string.command_cease_fire, 3),
    UnloadWeapon(R.raw.patron_ur_proppa_vapen, R.string.command_unload_weapon, 4),
    Visitation(R.raw.visitation, R.string.command_inspection, 2),
    Mark(-1, R.string.command_mark, -1);
}
