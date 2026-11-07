package com.example.novelreader

import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.novelreader.util.DebugLogger

class DebugActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private lateinit var scroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        tvLog = findViewById(R.id.tvLog)
        scroll = findViewById(R.id.scrollLog)
        val btnRefresh = findViewById<Button>(R.id.btnRefresh)
        val btnClear = findViewById<Button>(R.id.btnClear)

        refreshLog()
        btnRefresh.setOnClickListener {
            refreshLog()
            Toast.makeText(this, "已刷新", Toast.LENGTH_SHORT).show()
        }
        btnClear.setOnClickListener {
            DebugLogger.clear(this)
            refreshLog()
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshLog() {
        tvLog.text = DebugLogger.getLogs(this)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}