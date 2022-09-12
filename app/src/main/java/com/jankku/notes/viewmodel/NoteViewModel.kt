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
    private val _eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventChannel = _eventChannel.receiveAsFlow()
    val pinnedNotes = dao.getNotes(true)
    val unpinnedNotes = dao.getNotes(false)
    val noteCount = dao.getNoteCount()
    val noteEdited = MutableLiveData(false)

    fun pin(noteId: Long, pinned: Boolean) =
        viewModelScope.launch(Dispatchers.IO) { dao.pin(noteId, pinned) }

    fun insertOrUpdate(
        noteId: Long?,
        title: String,
        body: String,
        timeInMs: Long,
        pinned: Boolean,
    ) {
        if (title.isEmpty() && body.isEmpty()) {
            sendEvent(Event.NavigateUp)
            return
        } else if (noteEdited.value == false) {
            sendEvent(Event.NavigateUp)
            return
        }

        when (noteId) {
            null -> insert(Note(0, title, body, timeInMs, null, false))
            else -> update(noteId, title, body, timeInMs, pinned)
        }

        sendEvent(Event.NavigateUp)
    }

    private fun insert(note: Note) = viewModelScope.launch(Dispatchers.IO) { dao.insert(note) }

    private fun update(id: Long, title: String, body: String, editedOn: Long, pinned: Boolean) =
        viewModelScope.launch(Dispatchers.IO) { dao.update(id, title, body, editedOn, pinned) }

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
