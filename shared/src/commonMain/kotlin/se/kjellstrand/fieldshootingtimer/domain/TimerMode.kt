package se.kjellstrand.fieldshootingtimer.domain

/**
 * How a run begins. [Training] starts the command sequence immediately.
 * [Competition] prefixes it with the "Ladda?", "Alla klara?" and
 * "10 sekunder kvar?" dialogs — each confirmation makes the call and
 * advances, the last one starting the timed sequence.
 */
enum class TimerMode {
    Training,
    Competition
}
