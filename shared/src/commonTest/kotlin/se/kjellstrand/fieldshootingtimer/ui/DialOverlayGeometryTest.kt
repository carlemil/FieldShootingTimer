package se.kjellstrand.fieldshootingtimer.ui

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialOverlayGeometryTest {

    @Test
    fun `sweepAngles are proportional and fill the available angle`() {
        val segments = listOf(7f, 3f, 5f, 3f, 4f, 2f) // total 24
        val sweeps = sweepAngles(segments, gapAngleDegrees = 30f)
        assertEquals(segments.size, sweeps.size)
        assertEquals(330f, sweeps.sum(), 1e-3f)
        assertEquals(sweeps[0] / 7f, sweeps[2] / 5f, 1e-4f)
    }

    @Test
    fun `sweepAngles of an all-zero segment list are zero rather than NaN`() {
        assertEquals(listOf(0f, 0f), sweepAngles(listOf(0f, 0f), 30f))
    }

    @Test
    fun `perSecondTickSeconds skips segment boundaries`() {
        val segments = listOf(7f, 3f, 5f, 3f, 4f, 2f) // boundaries 7,10,15,18,22,24
        val ticks = perSecondTickSeconds(segments)
        val boundaries = listOf(7f, 10f, 15f, 18f, 22f, 24f)
        boundaries.forEach { b ->
            assertTrue(b !in ticks, "boundary $b must not get a per-second tick")
        }
        assertTrue(1f in ticks)
        assertTrue(23f in ticks)
    }

    @Test
    fun `perSecondTickSeconds tolerates float accumulation on boundaries`() {
        // 0.1f sums drift from exact integers; the epsilon comparison must
        // still suppress the tick at the boundary.
        val segments = List(10) { 0.7f } // boundaries 0.7, 1.4, ..., 7.0f-ish
        val ticks = perSecondTickSeconds(segments)
        assertTrue(ticks.none { it == 7f }, "accumulated-float boundary ~7.0 must be suppressed, got $ticks")
    }

    @Test
    fun `perSecondTickSeconds keeps integer ticks around fractional boundaries`() {
        val segments = listOf(7f, 3f, 2.5f, 3f, 4f, 2f) // boundary at 12.5
        val ticks = perSecondTickSeconds(segments)
        assertTrue(12f in ticks)
        assertTrue(13f in ticks)
    }

    @Test
    fun `polarToCartesian handles the cardinal directions`() {
        val center = Offset(100f, 100f)
        val right = polarToCartesian(center, 50f, 0f)
        assertEquals(150f, right.x, 1e-3f)
        assertEquals(100f, right.y, 1e-3f)
        val down = polarToCartesian(center, 50f, 90f)
        assertEquals(100f, down.x, 1e-3f)
        assertEquals(150f, down.y, 1e-3f)
        val up = polarToCartesian(center, 50f, 270f)
        assertEquals(100f, up.x, 1e-3f)
        assertEquals(50f, up.y, 1e-3f)
    }

    @Test
    fun `dialRadii derive from ring thickness and border width`() {
        val radii = dialRadii(canvasSizePx = 400f, ringThicknessPx = 32f, borderWidthPx = 4f)
        assertEquals(182f, radii.arcRadius, 1e-3f) // 200 - (16 + 2)
        assertEquals(202f, radii.outerBadgeRadius, 1e-3f) // 182 + 32/1.6
        assertEquals(162f, radii.innerBadgeRadius, 1e-3f) // 182 - 32/1.6
    }

    @Test
    fun `tickWedgeHalfWidthRadians scales with border width`() {
        val one = tickWedgeHalfWidthRadians(1f)
        assertTrue(one > 0f)
        assertEquals(one * 5f, tickWedgeHalfWidthRadians(5f), 1e-6f)
    }

    @Test
    fun `calculateSegmentAngles returns one divider per boundary including both ends`() {
        val sweeps = sweepAngles(listOf(7f, 3f, 5f, 3f, 4f, 2f), 30f)
        val angles = calculateSegmentAngles(sweeps, 30f)
        assertEquals(sweeps.size + 1, angles.size)
        assertEquals(DialGeometry.startAngle(30f), angles.first(), 1e-3f)
        // Last divider sits at startAngle + availableAngle (mod 360).
        assertEquals((DialGeometry.startAngle(30f) + 330f) % 360f, angles.last(), 1e-3f)
    }

    @Test
    fun `centerOnSegmentMarkerAngles places markers mid-segment`() {
        val sweeps = listOf(100f, 50f)
        val angles = centerOnSegmentMarkerAngles(sweeps, gapAngleDegrees = 30f)
        val start = DialGeometry.startAngle(30f)
        assertEquals((start + 50f) % 360f, angles[0], 1e-3f)
        assertEquals((start + 100f + 25f) % 360f, angles[1], 1e-3f)
    }
}
