package com.example.novelreader.tts

enum class TtsEngineType {
    SYSTEM,   // 新增
    SHERPA
}

interface TtsEngine {
    fun speak(text: String, speed: Float)
    fun stop()
    fun release()
}
