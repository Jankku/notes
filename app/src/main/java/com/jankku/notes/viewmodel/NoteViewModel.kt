package com.jankku.notes.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jankku.notes.db.NoteDao
import com.jankku.notes.db.model.Note
import com.jankku.notes.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class NoteViewModel(private val dao: NoteDao) : ViewModel() {
    val allNotes = dao.getNotes()
    val noteEdited = MutableLiveData(false)

    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventChannel = _eventChannel.receiveAsFlow()

    fun insertOrUpdate(
        noteId: Long?,
        title: String,
        body: String,
        timeInMs: Long,
    ) {
        if (title.isEmpty() && body.isEmpty()) {
            sendEvent(Event.NavigateUp(true))
            return
        } else if (noteEdited.value == false) {
            sendEvent(Event.NavigateUp(true))
            return
        }

        when (noteId) {
            null -> insert(Note(0, title, body, timeInMs, null))
            else -> update(noteId, title, body, timeInMs)
        }

        sendEvent(Event.NavigateUp(true))
    }

    private fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) { dao.insert(note) }

    private fun update(id: Long, title: String, body: String, editedOn: Long) =
        viewModelScope.launch(Dispatchers.IO) { dao.update(id, title, body, editedOn) }

    fun delete(noteId: Long) = viewModelScope.launch(Dispatchers.IO) { dao.delete(noteId) }

    fun sendEvent(event: Event) = viewModelScope.launch {
        _eventChannel.trySend(event)
    }
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