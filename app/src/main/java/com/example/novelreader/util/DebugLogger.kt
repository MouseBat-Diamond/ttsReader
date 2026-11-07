package com.example.novelreader.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    private const val PREF = "debug_prefs"
    private const val KEY_ENABLED = "debug_enabled"
    private const val LOG_FILE = "debug.log"

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) {
            // 开启时清空旧日志，方便观察本次会话
            logFile(context).writeText("")
        }
    }

    fun log(context: Context, tag: String, msg: String) {
        if (!isEnabled(context)) return
        val line = "${timeFormat.format(Date())} [$tag] $msg\n"
        logFile(context).appendText(line)
    }

    fun getLogs(context: Context): String {
        val file = logFile(context)
        return if (file.exists()) file.readText() else "（暂无日志）"
    }

    fun clear(context: Context) {
        logFile(context).writeText("")
    }

    private fun logFile(context: Context): File =
        File(context.filesDir, LOG_FILE)
}