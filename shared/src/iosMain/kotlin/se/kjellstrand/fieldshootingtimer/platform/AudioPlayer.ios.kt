package se.kjellstrand.fieldshootingtimer.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create
import se.kjellstrand.fieldshootingtimer.resources.Res
import se.kjellstrand.fieldshootingtimer.ui.Command

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
}

@OptIn(ExperimentalResourceApi::class)
class IosAudioPlayer : AudioPlayer {
    private val players: MutableMap<Command, AVAudioPlayer> = mutableMapOf()

    override suspend fun preload(cues: List<Command>) {
        cues.forEach { cue ->
            val path = cue.audioPath ?: return@forEach
            try {
                val data = Res.readBytes(path).toNSData()
                val player = AVAudioPlayer(data = data, error = null)
                player.prepareToPlay()
                players[cue] = player
            } catch (e: Exception) {
                // ignore failures; silent state matches Android behavior
            }
        }
    }

    override fun play(command: Command) {
        players[command]?.let { player ->
            player.setCurrentTime(0.0)
            player.play()
        }
    }

    override fun release() {
        players.values.forEach { it.stop() }
        players.clear()
    }
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    val player = remember { IosAudioPlayer() }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}
