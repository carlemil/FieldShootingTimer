package se.kjellstrand.fieldshootingtimer.domain

/**
 * How a run begins. [Training] starts the command sequence immediately.
 * [Competition] prefixes it with a 60s preparation countdown (modeled as
 * currentTime running from -[COMPETITION_COUNTDOWN_SECONDS] up to 0) during
 * which "Ladda!" and then "Alla klara!" are highlighted.
 */
enum class TimerMode {
    Training,
    Competition
}
