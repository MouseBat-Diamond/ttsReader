package com.example.novelreader.tts

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

object TtsPlayer {

    /** 播放 sherpa-onnx 生成的 float PCM 采样（假设 22050Hz 单声道） */
    fun play(samples: FloatArray, sampleRate: Int = 22050) {
        val bufferSize = samples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        val pcm = ShortArray(samples.size) { i ->
            (samples[i].coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        track.write(pcm, 0, pcm.size)
        track.play()
    }
}