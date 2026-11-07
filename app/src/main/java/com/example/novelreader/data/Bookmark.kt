package com.example.novelreader.data

data class Bookmark(
    val fileUri: String,
    val fileName: String,
    val offset: Int,
    val percent: Float,
    val createdAt: Long
)