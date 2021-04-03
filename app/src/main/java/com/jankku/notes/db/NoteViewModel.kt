package com.jankku.notes.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes: LiveData<List<Note>> = repository.allNotes.asLiveData()

    @WorkerThread
    fun insert(note: Note) = viewModelScope.launch { repository.insert(note) }

    @WorkerThread
    fun update(id: Long, title: String, body: String, editedOn: Long) =
        viewModelScope.launch { repository.update(id, title, body, editedOn) }

    @WorkerThread
    fun delete(noteId: Long) = viewModelScope.launch { repository.delete(noteId) }
}