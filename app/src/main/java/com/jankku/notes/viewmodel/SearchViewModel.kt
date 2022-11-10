package com.jankku.notes.viewmodel

import androidx.lifecycle.*
import com.jankku.notes.db.NoteDao
import com.jankku.notes.db.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(private val dao: NoteDao) : ViewModel() {
    private val _results = MutableLiveData<List<Note>>()
    val results get() = _results

    fun search(query: String?) = viewModelScope.launch(Dispatchers.IO) {
        val sanitizedQuery = sanitizeQuery("*$query*")
        val notes = dao.search(sanitizedQuery)
        _results.postValue(notes)
    }

    private fun sanitizeQuery(query: String?): String {
        if (query == null) return ""
        val queryWithEscapedQuotes = query.replace(Regex.fromLiteral("\""), "\"\"")
        return "\"$queryWithEscapedQuotes\""
    }
}

class SearchViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}