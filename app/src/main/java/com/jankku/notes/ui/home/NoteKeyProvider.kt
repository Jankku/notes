package com.jankku.notes.ui.home

import androidx.recyclerview.selection.ItemKeyProvider

class NoteKeyProvider(private val adapter: NoteAdapter) : ItemKeyProvider<Long>(SCOPE_MAPPED) {

    override fun getKey(position: Int): Long {
        return adapter.differ.currentList[position].id
    }

    override fun getPosition(key: Long): Int {
        return adapter.differ.currentList.indexOfFirst { it.id == key }
    }

}