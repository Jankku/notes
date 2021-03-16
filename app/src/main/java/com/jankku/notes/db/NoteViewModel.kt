package com.jankku.notes.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.*
import kotlinx.coroutines.launch

open class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes: LiveData<List<Note>> = repository.allNotes.asLiveData()

    @WorkerThread
    fun insert(note: Note) = viewModelScope.launch { repository.insert(note) }

    @WorkerThread
    fun update(note: Note) = viewModelScope.launch { repository.update(note) }

    @WorkerThread
    fun partialUpdate(id: Long, title: String, body: String, editedOn: Long) =
        viewModelScope.launch { repository.partialUpdate(id, title, body, editedOn) }

    @WorkerThread
    fun delete(noteId: Long) = viewModelScope.launch { repository.delete(noteId) }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}