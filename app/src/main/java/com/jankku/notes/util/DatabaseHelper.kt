package com.jankku.notes.util

import android.content.Context
import android.net.Uri
import com.jankku.notes.db.NoteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) {
    suspend fun exportDB(userChosenUri: Uri) = withContext(Dispatchers.IO) {
        try {
            NoteDatabase.destroyInstance()
            val inputStream: FileInputStream =
                context.getDatabasePath(NoteDatabase.DATABASE_NAME).inputStream()
            val outputStream = context.contentResolver.openOutputStream(userChosenUri)
            inputStream.use { input ->
                outputStream.use { output ->
                    if (output != null) {
                        input.copyTo(output)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    suspend fun importDB(userChosenUri: Uri) = withContext(Dispatchers.IO) {
        try {
            NoteDatabase.destroyInstance()
            val dbPath = context.getDatabasePath(NoteDatabase.DATABASE_NAME)
            val inputStream = context.contentResolver.openInputStream(userChosenUri)
            val outputStream = FileOutputStream(dbPath)
            val data =
                inputStream?.readBytes() ?: return@withContext Exception("Couldn't find file")
            outputStream.write(data, 0, data.size)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}