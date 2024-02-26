package org.openvoipalliance.androidphoneintegration.notifications

import android.content.Context
import android.media.*
import android.net.Uri
import android.provider.Settings
import org.openvoipalliance.androidphoneintegration.PIL
import org.openvoipalliance.androidphoneintegration.logWithContext
import org.openvoipalliance.androidphoneintegration.logging.LogLevel
import org.openvoipalliance.androidphoneintegration.logging.LogLevel.ERROR

class IncomingCallRinger(
    private val context: Context,
    private val pil: PIL,
    private val manager: AudioManager,
) : MediaPlayer.OnPreparedListener {

    private var player: MediaPlayer? = null

    private val handler = AudioFocusHandler()

    private val ringtone: Uri
        get() = when (pil.preferences.useApplicationProvidedRingtone) {
            true -> Uri.parse("android.resource://${context.packageName}/raw/ringtone")
            false -> Settings.System.DEFAULT_RINGTONE_URI
        }

    private val isOnSilentOrVibrate
        get() = manager.ringerMode != AudioManager.RINGER_MODE_NORMAL

    /**
     * Starts playing the phone's ringtone if that is what the user has chosen.
     */
    fun start() {
        if (isOnSilentOrVibrate) {
            log("The user's phone is not set to ring, not doing anything.")
            return
        }

        if (player != null || isStarted) {
            log("The ringtone is already playing, not doing anything.")
            return
        }

        player = MediaPlayer()

        requestAudioFocus()

        log("Starting ringer: $ringtone")

        player?.findRingtone()?.apply {
            setOnPreparedListener(this@IncomingCallRinger)
            setAudioAttributes(ringAttributes)
            isLooping = true
            prepareAsync()
        }
    }

    private val ringAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    private val audioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE).run {
            setAudioAttributes(ringAttributes)
            setAcceptsDelayedFocusGain(false)
            setOnAudioFocusChangeListener(this@IncomingCallRinger.handler)
            build()
        }
    }

    private fun requestAudioFocus() = manager.requestAudioFocus(audioFocusRequest)

    override fun onPrepared(mp: MediaPlayer) {
        try {
            mp.start()
        } catch (e: IllegalStateException) {
            log("Error: ${e.localizedMessage}")
        }
    }

    /**
     * Attempts to intelligently find the correct ringtone to be playing, if none is found,
     * null will be returned.
     */
    private fun MediaPlayer.findRingtone(): MediaPlayer? {
        try {
            log("Attempting to use the ringtone uri: $ringtone", LogLevel.WARNING)
            setDataSource(context, ringtone)
            return this
        } catch (e: Exception) {
        }

        try {
            log("Unable to use the DEFAULT_RINGTONE_URI, falling back to finding the first stored ringtone")
            RingtoneManager(context).apply {
                setType(RingtoneManager.TYPE_RINGTONE)
            }.let {
                it.cursor
                setDataSource(context, it.getRingtoneUri(1))
            }
            return this
        } catch (e: Exception) {
            log(
                "Unable to find any usable ringtone, we will not be playing anything",
                ERROR,
            )
        }

        return null
    }

    /**
     * Stop playing the phone's ringtone.
     */
    fun stop() {
        try {
            player?.apply {
                log("Stopping ringer")
                stop()
                release()
            }
            player = null
            manager.abandonAudioFocusRequest(audioFocusRequest)
        } catch (e: Exception) {
            log("Unable to stop ringer: " + e.message, ERROR)
        }
    }

    private val isStarted
        get() = player?.isPlaying ?: false

    private fun log(message: String, level: LogLevel = LogLevel.INFO) =
        logWithContext(message, "INCOMING-CALL-RINGER", level)

    inner class AudioFocusHandler : AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            log("Received audio focus change event: $focusChange")
        }
    }
}