package se.kjellstrand.fieldshootingtimer.domain

/**
 * How a run begins. [Training] starts the command sequence immediately.
 * [Competition] prefixes it with the "Ladda?" and "Alla klara?" dialogs —
 * each confirmation makes the call and advances — then runs the sequence
 * after a short silent gap (currentTime -[COMPETITION_ALL_READY_GAP_SECONDS]..0).
 */
enum class TimerMode {
    Training,
    Competition
}
