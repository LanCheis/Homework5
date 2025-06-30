package com.homework.photoapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class PhotoEntity(
    val id: Long = 0,
    val uri: String,
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "homework5_photos.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_PHOTOS = "photos"

        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_URI = "uri"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_PHOTOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_URI TEXT NOT NULL,
                $COLUMN_TITLE TEXT DEFAULT '',
                $COLUMN_DESCRIPTION TEXT DEFAULT '',
                $COLUMN_CREATED_AT INTEGER DEFAULT (strftime('%s','now'))
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PHOTOS")
        onCreate(db)
    }

    fun insertPhoto(photo: PhotoEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_URI, photo.uri)
            put(COLUMN_TITLE, photo.title)
            put(COLUMN_DESCRIPTION, photo.description)
            put(COLUMN_CREATED_AT, photo.createdAt)
        }

        val id = db.insert(TABLE_PHOTOS, null, values)
        db.close()
        return id
    }

    fun getAllPhotos(): List<PhotoEntity> {
        val photos = mutableListOf<PhotoEntity>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_PHOTOS,
            arrayOf(COLUMN_ID, COLUMN_URI, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_CREATED_AT),
            null, null, null, null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val photo = PhotoEntity(
                    id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                    uri = it.getString(it.getColumnIndexOrThrow(COLUMN_URI)),
                    title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                    description = it.getString(it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                )
                photos.add(photo)
            }
        }

        db.close()
        return photos
    }

    fun updatePhoto(photo: PhotoEntity): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, photo.title)
            put(COLUMN_DESCRIPTION, photo.description)
        }

        val rowsAffected = db.update(
            TABLE_PHOTOS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(photo.id.toString())
        )

        db.close()
        return rowsAffected
    }

    fun deletePhoto(id: Long): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(
            TABLE_PHOTOS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteAllPhotos(): Int {
        val db = writableDatabase
        val rowsAffected = db.delete(TABLE_PHOTOS, null, null)
        db.close()
        return rowsAffected
    }

    // Method to get database path for SQLite commands
    fun getDatabasePath(): String {
        return readableDatabase.path ?: ""
    }
}
