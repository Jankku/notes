package com.jankku.notes.ui.home

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.SelectionTracker
import com.jankku.notes.R
import com.jankku.notes.viewmodel.NoteViewModel

// https://stackoverflow.com/questions/62883686/contextual-action-bar-with-androidx-navigation-component
class ActionModeCallback : ActionMode.Callback {
    private var mode: ActionMode? = null
    private var title: String? = null
    private var selectionTracker: SelectionTracker<Long>? = null
    private var noteViewModel: NoteViewModel? = null
    private var adapter: NoteAdapter? = null
    private var noteList: MutableList<Long>? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        this.mode = mode
        mode.menuInflater?.inflate(R.menu.menu_selection, menu)
        mode.title = title
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val noteIdList = this.selectionTracker!!.selection.toList()

        when (item?.itemId) {
            R.id.action_select_all -> {
                selectionTracker!!.setItemsSelected(noteList!!.asIterable(), true)
            }
            R.id.action_delete -> {
                Thread {
                    for (id in noteIdList) {
                        noteViewModel?.delete(id)
                    }
                }.start()
                this.mode?.finish()
            }
            else -> this.mode?.finish()
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        this.mode = null
    }

    fun startActionMode(
        view: FragmentActivity,
        selectionTracker: SelectionTracker<Long>,
        noteViewModel: NoteViewModel,
        adapter: NoteAdapter,
        noteList: MutableList<Long>,
        title: String?
    ): ActionMode? {
        this.title = title
        this.selectionTracker = selectionTracker
        this.noteViewModel = noteViewModel
        this.adapter = adapter
        this.noteList = noteList
        return view.startActionMode(this)
    }
}