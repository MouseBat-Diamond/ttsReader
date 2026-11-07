package com.example.novelreader.data

import android.content.Context

object ProgressStore {
    private const val PREF = "reader_progress"

    fun save(context: Context, key: String, offset: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(key, offset).apply()
    }

    fun load(context: Context, key: String): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(key, 0)
}