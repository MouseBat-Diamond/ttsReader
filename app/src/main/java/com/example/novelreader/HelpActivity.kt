package com.example.novelreader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.novelreader.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvHelp.text = HELP_TEXT
    }

    companion object {
        private val HELP_TEXT = """
            离线 TTS 模型使用说明

            1. 下载模型（任选其一，可同时放置多个）：
            · Matcha 中文模型（约72MB）：
              https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/matcha-icefall-zh-baker.tar.bz2
            · Kokoro int8 多语言模型：
              https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-int8-multi-lang-v1_1.tar.bz2

            2. 解压：
            用手机上的解压工具（如 ZArchiver）解压 .tar.bz2 文件，得到一个文件夹。

            3. 放置目录（二选一）：
            · /sdcard/Download/NovelReader/tts_models/
            · /sdcard/Android/data/com.example.novelreader/files/tts_models/
            把解压后的整个文件夹放进上述目录，确保文件夹内包含 .onnx 模型文件和 tokens.txt。

            4. 在“设置”页点击“扫描模型目录”，列表中出现模型后点击选择即可使用。

            5. 若关屏后朗读被中断，请在系统设置中为本应用关闭电池优化、允许后台活动。

            提示：系统 TTS 对停顿时间的精细控制有限，离线模型效果更佳；语速采用 1-128 档映射到底层参数。
        """.trimIndent()
    }
}