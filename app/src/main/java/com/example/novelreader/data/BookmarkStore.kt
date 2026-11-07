package com.example.novelreader.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object BookmarkStore {
    private const val PREF = "bookmarks"
    private const val KEY = "list"

    fun loadAll(context: Context): MutableList<Bookmark> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val list = mutableListOf<Bookmark>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Bookmark(
                        fileUri = o.getString("fileUri"),
                        fileName = o.getString("fileName"),
                        offset = o.getInt("offset"),
                        percent = o.getDouble("percent").toFloat(),
                        createdAt = o.getLong("createdAt")
                    )
                )
            }
        } catch (_: Exception) { }
        return list
    }

    fun saveAll(context: Context, list: List<Bookmark>) {
        val arr = JSONArray()
        list.forEach { b ->
            arr.put(JSONObject().apply {
                put("fileUri", b.fileUri)
                put("fileName", b.fileName)
                put("offset", b.offset)
                put("percent", b.percent.toDouble())
                put("createdAt", b.createdAt)
            })
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, bookmark: Bookmark) {
        val list = loadAll(context)
        list.add(bookmark)
        saveAll(context, list)
    }

    fun remove(context: Context, bookmark: Bookmark) {
        val list = loadAll(context)
        list.removeAll { it.fileUri == bookmark.fileUri && it.offset == bookmark.offset && it.createdAt == bookmark.createdAt }
        saveAll(context, list)
    }

    fun removeAll(context: Context) {
        saveAll(context, emptyList())
    }
}