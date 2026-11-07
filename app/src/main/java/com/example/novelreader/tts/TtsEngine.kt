package com.example.novelreader.tts

interface TtsEngine {
    fun speak(text: String, speed: Float)
    fun stop()
    fun release()
}