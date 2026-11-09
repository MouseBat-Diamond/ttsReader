package com.example.novelreader.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class SystemTtsEngine(
    private val context: Context,
    private val onDone: (() -> Unit)? = null
) : TtsEngine, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false
    private var pendingText: String? = null
    private var pendingSpeed = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            tts?.language = Locale.CHINA
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) { onDone?.invoke() }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
            pendingText?.let { speak(it, pendingSpeed) }
            pendingText = null
        }
    }

    override fun speak(text: String, speed: Float) {
        val engine = tts ?: return
        if (!ready) {
            pendingText = text
            pendingSpeed = speed
            return
        }
        engine.setSpeechRate(speed.coerceIn(0.1f, 2.0f))
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt_${System.currentTimeMillis()}")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
