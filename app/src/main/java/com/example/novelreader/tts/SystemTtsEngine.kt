package com.example.novelreader.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SystemTtsEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var ready = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            ready = true
        }
    }

    fun speak(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utt")
    }

    fun stop() { tts?.stop() }

    fun release() { tts?.shutdown(); tts = null }
}