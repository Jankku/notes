package com.jankku.notes.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAll()

    @WorkerThread
    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    @WorkerThread
    suspend fun update(id: Long, title: String, body: String, editedOn: Long) {
        noteDao.update(id, title, body, editedOn)
    }

    @WorkerThread
    suspend fun delete(noteId: Long) {
        noteDao.delete(noteId)
    }
}