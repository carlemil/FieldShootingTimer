package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DialDragGeometryTest {

    private val gap = 30f

    @Test
    fun `angleFromCenterDegrees maps the four compass points`() {
        val center = Offset(100f, 100f)
        assertEquals(0f, angleFromCenterDegrees(center, Offset(150f, 100f)), 1e-3f)
        assertEquals(90f, angleFromCenterDegrees(center, Offset(100f, 150f)), 1e-3f)
        assertEquals(180f, angleFromCenterDegrees(center, Offset(50f, 100f)), 1e-3f)
        assertEquals(270f, angleFromCenterDegrees(center, Offset(100f, 50f)), 1e-3f)
    }

    @Test
    fun `dialAngleToTickValue is the inverse of tickAngle`() {
        val ticksMax = 24f
        for (tick in 0..24) {
            val angle = DialGeometry.tickAngle(tick.toFloat(), ticksMax, gap)
            val normalized = ((angle % 360f) + 360f) % 360f
            val roundTripped = dialAngleToTickValue(normalized, ticksMax, gap)
            assertTrue(
                abs(roundTripped - tick) < 1e-3f,
                "tick $tick round-tripped to $roundTripped"
            )
        }
    }

    @Test
    fun `angles in the bottom gap snap to the nearer end`() {
        // Gap 30 => dial spans 105..435 degrees; the gap straddles 90 degrees.
        val ticksMax = 24f
        // 5 degrees before the dial's start: snaps to 0.
        assertEquals(0f, dialAngleToTickValue(100f, ticksMax, gap), 1e-3f)
        // 5 degrees past the dial's end (435 % 360 = 75): snaps to max.
        assertEquals(ticksMax, dialAngleToTickValue(80f, ticksMax, gap), 1e-3f)
    }

    @Test
    fun `nearestTickIndex picks the closest tick within tolerance`() {
        val ticks = listOf(12f, 14f, 16f)
        assertEquals(1, nearestTickIndex(14.4f, ticks, toleranceSeconds = 1f))
        assertEquals(2, nearestTickIndex(15.2f, ticks, toleranceSeconds = 1f))
    }

    @Test
    fun `nearestTickIndex returns null when nothing is close enough`() {
        assertNull(nearestTickIndex(10f, listOf(12f, 14f), toleranceSeconds = 1f))
        assertNull(nearestTickIndex(10f, emptyList(), toleranceSeconds = 1f))
    }

    @Test
    fun `ring band accepts the ring and its tick blocks but not the dial face`() {
        val canvasSize = 300f
        val ringThickness = 60f
        // Center and inner dial face: rejected.
        assertTrue(!isWithinRingBand(0f, canvasSize, ringThickness))
        assertTrue(!isWithinRingBand(50f, canvasSize, ringThickness))
        // On the ring and just outside it (tick blocks): accepted.
        assertTrue(isWithinRingBand(120f, canvasSize, ringThickness))
        assertTrue(isWithinRingBand(150f, canvasSize, ringThickness))
        assertTrue(isWithinRingBand(170f, canvasSize, ringThickness))
        // Far outside: rejected.
        assertTrue(!isWithinRingBand(250f, canvasSize, ringThickness))
    }

    @Test
    fun `tickDragToleranceSeconds converts arc pixels to seconds`() {
        // Full available arc length at radius r spans ticksMax seconds, so a
        // slop of the whole arc must equal ticksMax.
        val ticksMax = 24f
        val radius = 150f
        val availRad = DialGeometry.availableAngle(gap) * (kotlin.math.PI.toFloat() / 180f)
        val fullArcPx = availRad * radius
        assertEquals(
            ticksMax,
            tickDragToleranceSeconds(fullArcPx, radius, ticksMax, gap),
            1e-3f
        )
        // Tolerance scales linearly with the slop.
        assertEquals(
            ticksMax / 10f,
            tickDragToleranceSeconds(fullArcPx / 10f, radius, ticksMax, gap),
            1e-3f
        )
    }
}
