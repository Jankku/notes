package com.jankku.notes.ui

import android.content.Context
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.selection.SelectionTracker
import com.jankku.notes.R
import com.jankku.notes.db.NoteViewModel

class ActionModeCallback : ActionMode.Callback {
    private var mode: ActionMode? = null
    private var title: String? = null
    private var selectionTracker: SelectionTracker<Long>? = null
    private var noteViewModel: NoteViewModel? = null
    private var adapter: NoteAdapter? = null
    private var context: Context? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        this.mode = mode
        mode.menuInflater?.inflate(R.menu.menu_selection, menu)
        mode.title = title
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val noteIdList = selectionTracker!!.selection.toList()

        when (item?.itemId) {
            R.id.action_delete -> {
                Thread {
                    for (id in noteIdList) {
                        noteViewModel?.delete(id)
                    }
                }.start()
                mode?.finish()
            }
            else -> mode?.finish()
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        this.mode = null
    }

    fun startActionMode(
        view: View,
        context: Context?,
        selectionTracker: SelectionTracker<Long>,
        noteViewModel: NoteViewModel,
        adapter: NoteAdapter,
        title: String? = null
    ) {
        this.title = title
        this.selectionTracker = selectionTracker
        this.noteViewModel = noteViewModel
        this.adapter = adapter
        this.context = context
        view.startActionMode(this)
    }
}