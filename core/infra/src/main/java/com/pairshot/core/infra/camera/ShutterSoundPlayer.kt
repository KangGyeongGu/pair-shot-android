package com.pairshot.core.infra.camera

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.File

internal class ShutterSoundPlayer(
    private val context: Context,
) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val player: MediaPlayer? =
        run {
            val candidates =
                listOf(
                    "/system/media/audio/ui/camera_click.ogg",
                    "/system/media/audio/ui/camera_shutter.ogg",
                    "/system/media/audio/ui/CameraClick.ogg",
                )
            val path = candidates.firstOrNull { File(it).exists() }
            path?.let {
                runCatching {
                    MediaPlayer().apply {
                        setDataSource(it)
                        prepare()
                    }
                }.getOrNull()
            }
        }

    fun play() {
        val p = player ?: return
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val ratio = if (max > 0) current.toFloat() / max else 0f
        val vol = ratio * 0.10f
        p.setVolume(vol, vol)
        if (p.isPlaying) p.seekTo(0) else p.start()
    }

    fun release() {
        player?.release()
    }
}
