package se.kjellstrand.fieldshootingtimer.ui

object DialGeometry {
    const val TOP_ANGLE_DEG = 270f
    const val FULL_CIRCLE_DEG = 360f

    fun availableAngle(gapDegrees: Float): Float = FULL_CIRCLE_DEG - gapDegrees

    fun startAngle(gapDegrees: Float): Float = TOP_ANGLE_DEG - availableAngle(gapDegrees) / 2

    fun tickAngle(tick: Float, ticksMax: Float, gapDegrees: Float): Float {
        val avail = availableAngle(gapDegrees)
        return TOP_ANGLE_DEG - avail / 2 + (tick / ticksMax) * avail
    }
}
