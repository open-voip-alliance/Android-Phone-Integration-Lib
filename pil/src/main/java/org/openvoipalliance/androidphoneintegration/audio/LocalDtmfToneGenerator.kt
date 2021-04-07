package org.openvoipalliance.androidphoneintegration.audio

import android.media.AudioManager
import android.media.ToneGenerator
import java.util.*
import kotlin.concurrent.schedule

class LocalDtmfToneGenerator(private val audioManager: AudioManager) {

    private val volume: Int
        get() = audioManager.getStreamVolume(STREAM)

    /**
     * We use a timer to release the tone generator after the tone has finished,
     * this will be cancelled every time a new tone has been requested.
     *
     */
    private var timer: TimerTask? = null

    fun play(tone: Char) {
        val toneGenerator = ToneGenerator(STREAM, volume)

        toneGenerator.startTone(convertCharacterToTone(tone), TONE_DURATION)

        timer = Timer().schedule((TONE_DURATION + 500).toLong()) {
            toneGenerator.release()
        }
    }

    private fun convertCharacterToTone(tone: Char): Int = when(tone) {
        '0' -> ToneGenerator.TONE_DTMF_0
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        'a' -> ToneGenerator.TONE_DTMF_A
        'b' -> ToneGenerator.TONE_DTMF_B
        'c' -> ToneGenerator.TONE_DTMF_C
        'd' -> ToneGenerator.TONE_DTMF_D
        'p' -> ToneGenerator.TONE_DTMF_P
        's' -> ToneGenerator.TONE_DTMF_S
        else -> ToneGenerator.TONE_DTMF_0
    }

    companion object {
        const val TONE_DURATION = 250
        const val STREAM = AudioManager.STREAM_VOICE_CALL
    }
}