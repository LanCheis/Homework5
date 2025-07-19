package com.example.homework5

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "photos.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                image BLOB,
                title TEXT,
                description TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS photos")
        onCreate(db)
    }

    fun insert(photo: PhotoEntity): Long {
        val values = ContentValues().apply {
            put("image", bitmapToBytes(photo.image))
            put("title", photo.title)
            put("description", photo.description)
        }
        return writableDatabase.insert("photos", null, values)
    }

    fun update(photo: PhotoEntity): Int {
        val values = ContentValues().apply {
            put("title", photo.title)
            put("description", photo.description)
        }
        return writableDatabase.update(
            "photos", values, "id = ?", arrayOf(photo.id.toString())
        )
    }

    fun delete(id: Int): Int {
        return writableDatabase.delete("photos", "id = ?", arrayOf(id.toString()))
    }

    fun getAll(): List<PhotoEntity> {
        val cursor = readableDatabase.query("photos", null, null, null, null, null, "id DESC")
        val list = mutableListOf<PhotoEntity>()
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val imgBytes = it.getBlob(it.getColumnIndexOrThrow("image"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val desc = it.getString(it.getColumnIndexOrThrow("description"))
                val bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
                list.add(PhotoEntity(id, bitmap, title, desc))
            }
        }
        return list
    }

    private fun bitmapToBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
