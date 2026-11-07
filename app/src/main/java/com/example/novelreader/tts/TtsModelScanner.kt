package com.example.novelreader.tts

import java.io.File

data class TtsModel(val name: String, val dir: File, val modelFile: File)

object TtsModelScanner {

    // 用户放置模型的根目录（可根据实际调整）
    fun scanRoots(): List<File> {
        val roots = mutableListOf<File>()
        roots.add(File("/sdcard/Download/NovelReader/tts_models"))
        roots.add(File("/sdcard/Android/data/com.example.novelreader/files/tts_models"))
        return roots.filter { it.exists() && it.isDirectory }
    }

    fun scan(): List<TtsModel> {
        val result = mutableListOf<TtsModel>()
        scanRoots().forEach { root ->
            root.listFiles()?.forEach { dir ->
                if (!dir.isDirectory) return@forEach
                val onnx = dir.listFiles()?.firstOrNull {
                    it.name.endsWith(".onnx") && !it.name.contains("vocoder")
                }
                val tokens = File(dir, "tokens.txt")
                if (onnx != null && tokens.exists()) {
                    result.add(TtsModel(dir.name, dir, onnx))
                }
            }
        }
        return result
    }
}