package se.kjellstrand.fieldshootingtimer.persistence

/**
 * Persistence seam for user-configurable timer settings. Real DataStore-backed
 * implementation lands in task shared/persistence-datastore. For now this is
 * an interface with no implementations wired in.
 */
interface SettingsStore {
    suspend fun loadShootingDuration(): Float?
    suspend fun saveShootingDuration(value: Float)
    suspend fun loadThumbValues(): List<Float>?
    suspend fun saveThumbValues(values: List<Float>)
}
