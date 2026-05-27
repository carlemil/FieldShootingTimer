package se.kjellstrand.fieldshootingtimer.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class DialGeometryTest {

    private val eps = 0.001f

    @Test
    fun `availableAngle subtracts gap from full circle`() {
        assertEquals(360f, DialGeometry.availableAngle(0f), eps)
        assertEquals(330f, DialGeometry.availableAngle(30f), eps)
        assertEquals(270f, DialGeometry.availableAngle(90f), eps)
    }

    @Test
    fun `startAngle centers the arc opening at the bottom`() {
        assertEquals(90f, DialGeometry.startAngle(0f), eps)
        assertEquals(105f, DialGeometry.startAngle(30f), eps)
        assertEquals(135f, DialGeometry.startAngle(90f), eps)
    }

    @Test
    fun `tickAngle at tick=0 equals startAngle`() {
        assertEquals(
            DialGeometry.startAngle(30f),
            DialGeometry.tickAngle(0f, 17f, 30f),
            eps
        )
    }

    @Test
    fun `tickAngle at tick=ticksMax equals startAngle plus availableAngle`() {
        val gap = 30f
        val expected = DialGeometry.startAngle(gap) + DialGeometry.availableAngle(gap)
        assertEquals(expected, DialGeometry.tickAngle(17f, 17f, gap), eps)
    }

    @Test
    fun `tickAngle is linear in tick`() {
        val gap = 30f
        val ticksMax = 10f
        val start = DialGeometry.startAngle(gap)
        val avail = DialGeometry.availableAngle(gap)
        assertEquals(start + avail / 2, DialGeometry.tickAngle(5f, ticksMax, gap), eps)
    }

    @Test
    fun `tickAngle computes concrete expected value`() {
        assertEquals(270f, DialGeometry.tickAngle(5f, 10f, 30f), eps)
    }

    @Test
    fun `TOP_ANGLE_DEG is 270 degrees`() {
        assertEquals(270f, DialGeometry.TOP_ANGLE_DEG)
    }

    @Test
    fun `FULL_CIRCLE_DEG is 360 degrees`() {
        assertEquals(360f, DialGeometry.FULL_CIRCLE_DEG)
    }
}
