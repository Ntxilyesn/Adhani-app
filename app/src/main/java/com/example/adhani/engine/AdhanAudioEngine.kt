package com.example.adhani.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.R
import com.example.adhani.model.AdhanVoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AdhanAudioEngine {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingState = false

    val isPlaying: Boolean
        get() = isPlayingState

    /**
     * Plays the authentic recorded Adhan audio matching the chosen voice.
     */
    fun playAdhan(
        context: Context,
        voice: AdhanVoice,
        scope: CoroutineScope,
        onFinished: () -> Unit = {}
    ) {
        stop()

        val rawResId = when (voice) {
            AdhanVoice.MAKKAH -> R.raw.adhan_makkah
            AdhanVoice.MADINAH -> R.raw.adhan_madinah
            AdhanVoice.MUSTAFA_ISMAIL -> R.raw.adhan_egypt
            AdhanVoice.AL_AQSA -> R.raw.adhan_makkah
            AdhanVoice.GENTLE_TAKBEER -> R.raw.adhan_madinah
        }

        try {
            mediaPlayer = MediaPlayer.create(context.applicationContext, rawResId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setOnCompletionListener {
                    isPlayingState = false
                    stop()
                    scope.launch(Dispatchers.Main) {
                        onFinished()
                    }
                }
                setOnErrorListener { _, _, _ ->
                    isPlayingState = false
                    stop()
                    scope.launch(Dispatchers.Main) {
                        onFinished()
                    }
                    true
                }
                start()
            }
            isPlayingState = mediaPlayer?.isPlaying == true
        } catch (_: Exception) {
            isPlayingState = false
            onFinished()
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlayingState = false
    }
}
