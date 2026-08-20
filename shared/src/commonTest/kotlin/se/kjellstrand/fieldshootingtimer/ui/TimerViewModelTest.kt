package se.kjellstrand.fieldshootingtimer.ui

import se.kjellstrand.fieldshootingtimer.domain.Command

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelTest {

    private lateinit var viewModel: TimerViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = TimerViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Fixed behavior: mutators must not fire cue/haptic events ---

    @Test
    fun `mutators do not emit cue or thumb-crossed events`() = runTest {
        val vm = TimerViewModel(externalScope = backgroundScope)
        val cues = mutableListOf<Command>()
        val crossings = mutableListOf<Float>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.cueEventsFlow.collect { cues += it }
        }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            vm.thumbCrossedFlow.collect { crossings += it }
        }
        runCurrent()

        vm.setShootingTime(7f)
        vm.setTimerState(TimerRunningState.Running)
        vm.setThumbValues(listOf(5f, 7f))
        vm.addNewThumbValue(4..12)
        runCurrent()

        assertTrue(cues.isEmpty(), "mutators must not emit cues, got $cues")
        assertTrue(crossings.isEmpty(), "mutators must not emit crossings, got $crossings")
    }

    // --- setShootingTime ---

    @Test
    fun `setShootingTime updates shootingDuration`() {
        viewModel.setShootingTime(7f)
        assertEquals(7f, viewModel.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `setShootingTime accepts zero`() {
        viewModel.setShootingTime(0f)
        assertEquals(0f, viewModel.uiStateFlow.value.shootingDuration)
    }

    @Test
    fun `setShootingTime throws on negative input`() {
        assertFailsWith<IllegalArgumentException> {
            viewModel.setShootingTime(-1f)
        }
    }

    @Test
    fun `setShootingTime drops thumbs that fall outside the new range`() {
        // shooting=10 → range = 11..(10+10+3-1) = 11..22
        viewModel.setShootingTime(10f)
        viewModel.setThumbValues(listOf(12f, 18f, 22f))

        // shrink to shooting=2 → range = 11..(2+10+3-1) = 11..14
        // 18 and 22 are out of range and must be pruned atomically.
        viewModel.setShootingTime(2f)

        assertEquals(listOf(12f), viewModel.uiStateFlow.value.thumbValues)
    }

    // --- setTimerState ---

    @Test
    fun `setTimerState drives timer state transitions`() {
        assertEquals(TimerRunningState.NotStarted, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Running)
        assertEquals(TimerRunningState.Running, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Stopped)
        assertEquals(TimerRunningState.Stopped, viewModel.uiStateFlow.value.timerRunningState)

        viewModel.setTimerState(TimerRunningState.Finished)
        assertEquals(TimerRunningState.Finished, viewModel.uiStateFlow.value.timerRunningState)
    }

    // --- setCurrentTime ---

    @Test
    fun `setCurrentTime updates currentTime`() {
        viewModel.setCurrentTime(12.5f)
        assertEquals(12.5f, viewModel.uiStateFlow.value.currentTime)
    }

    // --- setThumbValues ---

    @Test
    fun `setThumbValues replaces thumb list`() {
        viewModel.setThumbValues(listOf(3f, 5f, 7f))
        assertEquals(listOf(3f, 5f, 7f), viewModel.uiStateFlow.value.thumbValues)

        viewModel.setThumbValues(emptyList())
        assertEquals(emptyList(), viewModel.uiStateFlow.value.thumbValues)
    }

    // --- dropLastThumbValue ---

    @Test
    fun `dropLastThumbValue drops last element`() {
        viewModel.setThumbValues(listOf(1f, 2f, 3f))
        viewModel.dropLastThumbValue()
        assertEquals(listOf(1f, 2f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `dropLastThumbValue is a no-op on empty list`() {
        viewModel.setThumbValues(emptyList())
        viewModel.dropLastThumbValue()
        assertEquals(emptyList(), viewModel.uiStateFlow.value.thumbValues)
    }

    // --- addNewThumbValue ---

    @Test
    fun `addNewThumbValue starts at the center of the range`() {
        // range 4..12, center = (4 + 12) / 2 = 8
        viewModel.addNewThumbValue(4..12)
        assertEquals(listOf(8f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `addNewThumbValue finds next free spot outward when center is taken`() {
        // range 4..12, center = 8; with 8 already taken, scan (forward first)
        // distance=1 → forward=9 is free → picks 9
        viewModel.setThumbValues(listOf(8f))
        viewModel.addNewThumbValue(4..12)

        val thumbs = viewModel.uiStateFlow.value.thumbValues
        assertEquals(2, thumbs.size)
        assertTrue(thumbs.contains(8f), "expected 8f to remain")
        assertTrue(thumbs.contains(9f), "expected next free spot adjacent to center")
    }

    @Test
    fun `addNewThumbValue does not exceed range capacity`() {
        // range 4..6 → cap is range.last - range.first = 2
        val range = 4..6
        viewModel.addNewThumbValue(range)
        viewModel.addNewThumbValue(range)
        val sizeAtCap = viewModel.uiStateFlow.value.thumbValues.size
        assertEquals(2, sizeAtCap)

        viewModel.addNewThumbValue(range)
        assertEquals(
            sizeAtCap,
            viewModel.uiStateFlow.value.thumbValues.size,
            "third add should be refused once at capacity"
        )
    }

    // --- roundThumbValues ---

    @Test
    fun `roundThumbValues rounds every thumb to nearest int`() {
        viewModel.setThumbValues(listOf(3.2f, 4.7f, 6.4f))
        viewModel.roundThumbValues()
        assertEquals(listOf(3f, 5f, 6f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `roundThumbValues merges flags that land on the same second`() {
        // A flag dragged onto another: both round to 6, one survives.
        viewModel.setThumbValues(listOf(5.5f, 6.4f, 8f))
        viewModel.roundThumbValues()
        assertEquals(listOf(6f, 8f), viewModel.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `ceaseFireBeep defaults off and toggles via the setter`() {
        assertEquals(false, viewModel.uiStateFlow.value.ceaseFireBeep)
        viewModel.setCeaseFireBeep(true)
        assertEquals(true, viewModel.uiStateFlow.value.ceaseFireBeep)
        viewModel.setCeaseFireBeep(false)
        assertEquals(false, viewModel.uiStateFlow.value.ceaseFireBeep)
    }

    @Test
    fun `roundThumbValues on empty list stays empty`() {
        viewModel.setThumbValues(emptyList())
        viewModel.roundThumbValues()
        assertEquals(emptyList(), viewModel.uiStateFlow.value.thumbValues)
    }
}
