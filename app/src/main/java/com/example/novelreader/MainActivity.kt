package com.example.novelreader

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.novelreader.data.ProgressStore
import com.example.novelreader.databinding.ActivityMainBinding
import com.example.novelreader.tts.SystemTtsEngine
import com.example.novelreader.tts.TtsModelScanner
import com.example.novelreader.tts.TtsService
import com.example.novelreader.util.DebugLogger
import com.example.novelreader.util.TextLoader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentKey: String = ""
    private var systemTts: SystemTtsEngine? = null
    private var currentFileName: String = ""
    private var currentText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DebugLogger.log(this, "Main", "MainActivity 创建")
        binding.tvContent.movementMethod = ScrollingMovementMethod()

        applyReaderPrefs()

        binding.btnOpen.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
            startActivityForResult(intent, REQ_OPEN)
        }

        binding.btnTts.setOnClickListener {
            val prefs = getSharedPreferences(SettingsActivity.PREF, MODE_PRIVATE)
            val modelPath = prefs.getString(SettingsActivity.KEY_MODEL, null)
            DebugLogger.log(this, "Main", "点击朗读，模型路径=$modelPath")

            if (modelPath.isNullOrBlank()) {
                // 系统 TTS：读当前段落
                DebugLogger.log(this, "Main", "走系统 TTS 分支")
                if (systemTts == null) systemTts = SystemTtsEngine(this)
                val text = binding.tvContent.text.toString()
                if (text.isBlank()) {
                    Toast.makeText(this, "没有可朗读的内容", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val start = binding.tvContent.selectionStart.coerceAtLeast(0)
                val end = text.indexOf('。', start).takeIf { it > 0 } ?: text.length
                systemTts?.speak(text.substring(start, end + 1), 1.0f)
            } else {
                DebugLogger.log(this, "Main", "走离线引擎分支，启动 TtsService")
                startForegroundService(Intent(this, TtsService::class.java))
            }
        }

        binding.btnBookmark.setOnClickListener {
            if (currentKey.isEmpty()) {
                Toast.makeText(this, "请先打开一个文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val offset = binding.tvContent.layout?.let { layout ->
                val line = layout.getLineForVertical(binding.tvContent.scrollY)
                layout.getOffsetForHorizontal(line, 0f)
            } ?: 0
            val percent = if (currentText.isNotEmpty()) offset * 100f / currentText.length else 0f
            val intent = Intent(this, BookmarkActivity::class.java).apply {
                putExtra("fileUri", currentKey)
                putExtra("fileName", currentFileName)
                putExtra("offset", offset)
                putExtra("percent", percent)
            }
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        handleIntent(intent)
    }

    private fun applyReaderPrefs() {
        val prefs = getSharedPreferences(SettingsActivity.PREF, MODE_PRIVATE)
        binding.tvContent.textSize = prefs.getInt(SettingsActivity.KEY_FONT, 18).toFloat()
        val family = when (prefs.getInt(SettingsActivity.KEY_FAMILY, 0)) {
            1 -> Typeface.SERIF
            2 -> Typeface.MONOSPACE
            else -> Typeface.SANS_SERIF
        }
        binding.tvContent.typeface = Typeface.create(family, Typeface.NORMAL)
        val bg = prefs.getInt(SettingsActivity.KEY_BG, Color.WHITE)
        binding.tvContent.setBackgroundColor(bg)
        binding.tvContent.setTextColor(
            if (bg == Color.parseColor("#303030")) Color.WHITE else Color.BLACK
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        if (intent.action == Intent.ACTION_VIEW) intent.data?.let { loadUri(it) }
    }

    private fun loadUri(uri: Uri) {
        val text = TextLoader.loadText(this, uri)
        binding.tvContent.text = text
        currentKey = uri.toString()
        currentText = text
        currentFileName = uri.lastPathSegment ?: "未命名"
        val offset = ProgressStore.load(this, currentKey)
        binding.tvContent.post {
            val layout = binding.tvContent.layout ?: return@post
            val line = layout.getLineForOffset(offset.coerceIn(0, text.length))
            binding.tvContent.scrollTo(0, layout.getLineTop(line))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_OPEN && resultCode == RESULT_OK) {
            data?.data?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                loadUri(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentKey.isNotEmpty()) {
            val offset = binding.tvContent.layout?.let { layout ->
                val line = layout.getLineForVertical(binding.tvContent.scrollY)
                layout.getOffsetForHorizontal(line, 0f)
            } ?: 0
            ProgressStore.save(this, currentKey, offset)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        systemTts?.release()
    }

    companion object {
        private const val REQ_OPEN = 1001
    }
}
