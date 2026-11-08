package com.example.novelreader.tts

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
//import com.example.novelreader.util.DebugLogger
import android.util.Log

class SherpaTtsEngine(private val context: Context) : TtsEngine {

    private var tts: OfflineTts? = null




fun load(model: TtsModel): Boolean {
    release()
    return try {
        val modelDir = model.dir.absolutePath
        val vits = OfflineTtsVitsModelConfig(
            model = model.modelFile.absolutePath,
            lexicon = "$modelDir/lexicon.txt",
            tokens = "$modelDir/tokens.txt",
            dataDir = modelDir,
            dictDir = modelDir
        )
        val modelConfig = OfflineTtsModelConfig(vits = vits, numThreads = 2)
        val config = OfflineTtsConfig(model = modelConfig)
        tts = OfflineTts(config = config)
        Log.d("SherpaTts", "模型加载成功: ${model.name}")
        true
    } catch (e: Exception) {
        Log.d("SherpaTts", "模型加载失败: ${e.message}")
        false
    }
}








    override fun speak(text: String, speed: Float) {
        val engine = tts ?: return
        try {
            val audio = engine.generate(text = text, sid = 0, speed = speed)
            TtsPlayer.play(audio.samples)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stop() {
        // AudioTrack 为一次性播放，停止由下一次调用覆盖；如需强制停止可扩展 TtsPlayer
    }

    override fun release() {
        tts?.release()
        tts = null
    }
}
