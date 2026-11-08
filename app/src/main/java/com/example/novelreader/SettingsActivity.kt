package com.example.novelreader

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.novelreader.databinding.ActivitySettingsBinding
import com.example.novelreader.tts.TtsModel
import com.example.novelreader.tts.TtsModelScanner

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private var selected: TtsModel? = null
    private var scannedModels: List<TtsModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getSharedPreferences(PREF, MODE_PRIVATE)

        // 调试日志开关
        binding.switchDebug.isChecked = com.example.novelreader.util.DebugLogger.isEnabled(this)
        binding.switchDebug.setOnCheckedChangeListener { _, checked ->
            com.example.novelreader.util.DebugLogger.setEnabled(this, checked)
            com.example.novelreader.util.DebugLogger.log(this, "Debug", "调试日志 ${if (checked) "已开启" else "已关闭"}")
        }
        binding.btnOpenDebug.setOnClickListener {
            startActivity(android.content.Intent(this, DebugActivity::class.java))
        }

        binding.rvModels.layoutManager = LinearLayoutManager(this)
        binding.btnScan.setOnClickListener { scanModels() }

        // 语速 128 档
        binding.seekRate.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvRate.text = "档位: ${progress + 1}"
                saveInt(KEY_RATE, progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        binding.seekRate.progress = loadInt(KEY_RATE, 63)

        // 字号 12-36sp
        binding.seekFont.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = 12 + progress
                binding.tvFont.text = "字号: ${size}sp"
                saveInt(KEY_FONT, size)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        binding.seekFont.progress = loadInt(KEY_FONT, 18) - 12

        // 字体选择
        binding.rgFont.setOnCheckedChangeListener { _, checkedId ->
            val idx = when (checkedId) {
                R.id.rbFontSerif -> 1
                R.id.rbFontMono -> 2
                else -> 0
            }
            saveInt(KEY_FAMILY, idx)
        }
        when (loadInt(KEY_FAMILY, 0)) {
            1 -> binding.rbFontSerif.isChecked = true
            2 -> binding.rbFontMono.isChecked = true
            else -> binding.rbFontDefault.isChecked = true
        }

        // 背景颜色
        binding.btnBgWhite.setOnClickListener { saveInt(KEY_BG, Color.WHITE) }
        binding.btnBgSepia.setOnClickListener { saveInt(KEY_BG, Color.parseColor("#F5E6C8")) }
        binding.btnBgGreen.setOnClickListener { saveInt(KEY_BG, Color.parseColor("#CCE8CF")) }
        binding.btnBgDark.setOnClickListener { saveInt(KEY_BG, Color.parseColor("#303030")) }

        // 初始化当前 TTS 选择（无模型时默认系统 TTS）
        loadCurrentTtsSelection()

        scanModels()
    }

    private fun scanModels() {
        scannedModels = TtsModelScanner.scan()
        if (scannedModels.isEmpty()) {
            Toast.makeText(this, "未找到离线模型，将使用系统 TTS", Toast.LENGTH_LONG).show()
        }
        binding.rvModels.adapter = ModelAdapter(scannedModels) { model ->
            selected = model
            saveString(KEY_MODEL, model.dir.absolutePath)
            saveString(KEY_TTS_ENGINE, model.name)
            Toast.makeText(this, "已选择: ${model.name}", Toast.LENGTH_SHORT).show()
        }
        loadCurrentTtsSelection()
    }

    private fun buildTtsOptions(): List<Pair<String, String>> {
        val options = mutableListOf("system" to "系统 TTS")
        options.addAll(scannedModels.map { it.name to it.name })
        return options
    }

    private fun loadCurrentTtsSelection(): String {
        val saved = prefs.getString(KEY_TTS_ENGINE, null)
        val options = buildTtsOptions()
        return if (saved.isNullOrBlank() || options.none { it.first == saved }) {
            prefs.edit().putString(KEY_TTS_ENGINE, "system").apply()
            "system"
        } else {
            saved
        }
    }

    private fun saveInt(key: String, value: Int) =
        prefs.edit().putInt(key, value).apply()

    private fun loadInt(key: String, def: Int) = prefs.getInt(key, def)

    private fun saveString(key: String, value: String) =
        prefs.edit().putString(key, value).apply()

    private class ModelAdapter(
        private val items: List<TtsModel>,
        private val onClick: (TtsModel) -> Unit
    ) : RecyclerView.Adapter<ModelAdapter.VH>() {

        class VH(val binding: com.example.novelreader.databinding.ItemModelBinding) :
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val b = com.example.novelreader.databinding.ItemModelBinding
                .inflate(android.view.LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = items[position]
            holder.binding.tvName.text = m.name
            holder.binding.tvPath.text = m.dir.absolutePath
            holder.binding.root.setOnClickListener { onClick(m) }
        }

        override fun getItemCount() = items.size
    }

    companion object {
        const val PREF = "reader_prefs"
        const val KEY_RATE = "rate_degree"
        const val KEY_FONT = "font_size"
        const val KEY_FAMILY = "font_family"
        const val KEY_BG = "bg_color"
        const val KEY_MODEL = "tts_model_path"
        const val KEY_TTS_ENGINE = "tts_engine"
    }
}