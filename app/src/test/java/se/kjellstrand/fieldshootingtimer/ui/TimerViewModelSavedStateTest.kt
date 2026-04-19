package se.kjellstrand.fieldshootingtimer.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerViewModelSavedStateTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Fixed behavior: user settings survive via SavedStateHandle ---

    @Test
    fun `ViewModel initializes shootingDuration from SavedStateHandle`() = runTest {
        val handle = SavedStateHandle(mapOf("shooting_duration" to 7f))
        val vm = TimerViewModel(externalScope = backgroundScope, savedStateHandle = handle)
        assertEquals(7f, vm.uiStateFlow.value.shootingDuration, 0f)
    }

    @Test
    fun `ViewModel initializes thumbValues from SavedStateHandle`() = runTest {
        val handle = SavedStateHandle(mapOf("thumb_values" to floatArrayOf(11f, 13f)))
        val vm = TimerViewModel(externalScope = backgroundScope, savedStateHandle = handle)
        assertEquals(listOf(11f, 13f), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `setShootingTime persists to SavedStateHandle`() = runTest {
        val handle = SavedStateHandle()
        val vm = TimerViewModel(externalScope = backgroundScope, savedStateHandle = handle)
        vm.setShootingTime(11f)
        advanceUntilIdle()
        assertEquals(11f, handle.get<Float>("shooting_duration"))
    }

    @Test
    fun `setThumbValues persists to SavedStateHandle`() = runTest {
        val handle = SavedStateHandle()
        val vm = TimerViewModel(externalScope = backgroundScope, savedStateHandle = handle)
        vm.setThumbValues(listOf(9f, 12f))
        advanceUntilIdle()
        val persisted = handle.get<FloatArray>("thumb_values")
        assertEquals(listOf(9f, 12f), persisted?.toList())
    }

    // --- Guard tests: no-handle ViewModel works as before ---

    @Test
    fun `ViewModel without SavedStateHandle uses defaults`() {
        val vm = TimerViewModel()
        assertEquals(5f, vm.uiStateFlow.value.shootingDuration, 0f)
        assertEquals(emptyList<Float>(), vm.uiStateFlow.value.thumbValues)
    }

    @Test
    fun `ViewModel without SavedStateHandle still mutates state`() {
        val vm = TimerViewModel()
        vm.setShootingTime(9f)
        vm.setThumbValues(listOf(3f, 7f))
        assertEquals(9f, vm.uiStateFlow.value.shootingDuration, 0f)
        assertEquals(listOf(3f, 7f), vm.uiStateFlow.value.thumbValues)
    }
}
