package com.example.novelreader.util

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

object TextLoader {

    /** 先按 UTF-8 读，若出现明显乱码特征则回退 GBK */
    fun loadText(context: Context, uri: Uri): String {
        val utf8 = read(context, uri, Charsets.UTF_8)
        if (!looksLikeGarbled(utf8)) return utf8
        val gbk = read(context, uri, Charset.forName("GBK"))
        return if (looksLikeGarbled(gbk)) utf8 else gbk
    }

    private fun read(context: Context, uri: Uri, charset: Charset): String {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input, charset)).use { reader ->
                return reader.readText()
            }
        }
        return ""
    }

    private fun looksLikeGarbled(text: String): Boolean {
        if (text.isBlank()) return false
        val sample = text.take(2000)
        val bad = sample.count { it == '\uFFFD' || it == '?' }
        return bad > sample.length * 0.05
    }
}