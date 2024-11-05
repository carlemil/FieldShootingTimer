package se.kjellstrand.fieldshootingtimer.audio

import se.kjellstrand.fieldshootingtimer.R

enum class AudioCueType(val resId: Int) {
    TenSecondsLeft(R.raw.tio_sekunder_kvar),
    Ready(R.raw.fardiga),
    Fire(R.raw.eld),
    CeaseFire(R.raw.eld_upp_hor),
    UnloadWeapon(R.raw.patron_ur_proppa_vapen),
    Visitation(R.raw.visitation)
}