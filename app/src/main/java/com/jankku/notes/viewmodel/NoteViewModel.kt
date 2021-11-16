package com.jankku.notes.viewmodel

import androidx.lifecycle.*
import com.jankku.notes.db.Note
import com.jankku.notes.db.NoteDao
import kotlinx.coroutines.launch

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    val allNotes: LiveData<List<Note>> = noteDao.getAll().asLiveData()

    fun insert(note: Note) = viewModelScope.launch { noteDao.insert(note) }

    fun update(id: Long, title: String, body: String, editedOn: Long) =
        viewModelScope.launch { noteDao.update(id, title, body, editedOn) }

    fun delete(noteId: Long) = viewModelScope.launch { noteDao.delete(noteId) }
}

class NoteViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}