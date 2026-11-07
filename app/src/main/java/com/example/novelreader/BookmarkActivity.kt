package com.example.novelreader

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.novelreader.data.Bookmark
import com.example.novelreader.data.BookmarkStore
import java.util.Locale

class BookmarkActivity : AppCompatActivity() {

    private lateinit var adapter: BookmarkAdapter
    private var selected: Bookmark? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        val rv = findViewById<RecyclerView>(R.id.rvBookmarks)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = BookmarkAdapter(BookmarkStore.loadAll(this)) { b ->
            selected = b
        }
        rv.adapter = adapter

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val fileUri = intent.getStringExtra("fileUri")
            val fileName = intent.getStringExtra("fileName")
            val offset = intent.getIntExtra("offset", 0)
            val percent = intent.getFloatExtra("percent", 0f)
            if (fileUri.isNullOrEmpty() || fileName.isNullOrEmpty()) {
                Toast.makeText(this, "请先在阅读页打开一个文件", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val b = Bookmark(fileUri, fileName, offset, percent, System.currentTimeMillis())
            BookmarkStore.add(this, b)
            reload()
            Toast.makeText(this, "已添加书签", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val b = selected
            if (b == null) {
                Toast.makeText(this, "请先选中一个书签", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            BookmarkStore.remove(this, b)
            selected = null
            reload()
            Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnDeleteAll).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("删除所有书签")
                .setMessage("确定要删除全部书签吗？")
                .setPositiveButton("删除") { _, _ ->
                    BookmarkStore.removeAll(this)
                    selected = null
                    reload()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }

    private fun reload() {
        adapter = BookmarkAdapter(BookmarkStore.loadAll(this)) { b -> selected = b }
        findViewById<RecyclerView>(R.id.rvBookmarks).adapter = adapter
    }

    private class BookmarkAdapter(
        private val items: List<Bookmark>,
        private val onSelect: (Bookmark) -> Unit
    ) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

        class VH(val view: android.view.View, val tv: android.widget.TextView) :
            RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val tv = android.widget.TextView(parent.context).apply {
                setPadding(24, 32, 24, 32)
                textSize = 16f
            }
            return VH(tv, tv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val b = items[position]
            val name = String.format(Locale.getDefault(), "%s_%.2f%%", b.fileName, b.percent)
            holder.tv.text = name
            holder.view.setOnClickListener {
                onSelect(b)
                Toast.makeText(holder.view.context, "已选中: $name", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = items.size
    }
}