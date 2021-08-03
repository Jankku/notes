package com.jankku.notes

import android.app.Application
import com.jankku.notes.db.NoteDatabase

class NotesApplication : Application() {
    private val database by lazy { NoteDatabase.getDatabase(this) }
    val noteDao by lazy { database.noteDao() }
}