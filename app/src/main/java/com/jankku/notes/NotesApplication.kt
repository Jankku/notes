package com.jankku.notes

import android.app.Application
import com.jankku.notes.db.NoteDatabase
import com.jankku.notes.db.NoteRepository

class NotesApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
    val repository by lazy { NoteRepository(database.noteDao()) }
}