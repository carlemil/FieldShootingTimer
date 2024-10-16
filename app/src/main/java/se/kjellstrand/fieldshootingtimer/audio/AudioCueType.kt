package se.kjellstrand.fieldshootingtimer.audio

import se.kjellstrand.fieldshootingtimer.R

enum class AudioCueType(val resId: Int) {
    TenSecondsLeft(R.raw.tio_sekunder_kvar_cut),
    Ready(R.raw.fardiga_cut),
    Fire(R.raw.eld_cut),
    CeaseFire(R.raw.eld_upp_hor_cut),
    UnloadWeapon(R.raw.patron_ur_proppa_vapen_cut)
}