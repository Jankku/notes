package com.jankku.notes.ui.home

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.SelectionTracker
import com.jankku.notes.R
import com.jankku.notes.viewmodel.NoteViewModel

class ActionModeCallback : ActionMode.Callback {
    private var mode: ActionMode? = null
    private var title: String? = null
    private var selectionTracker: SelectionTracker<Long>? = null
    private var viewModel: NoteViewModel? = null
    private var adapter: NoteAdapter? = null
    private lateinit var onDeleteCallback: (Int) -> Unit

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        this.mode = mode
        mode.menuInflater?.inflate(R.menu.menu_selection, menu)
        mode.title = title
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        this.mode?.title = title
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val selectedNoteIds = this.selectionTracker?.selection?.toList()
        val noteIdList = adapter?.currentList?.toList()?.map { it.id }?.asIterable()

        when (item?.itemId) {
            R.id.action_select_all -> {
                if (noteIdList != null) selectionTracker?.setItemsSelected(noteIdList, true)
            }
            R.id.action_delete -> {
                if (selectedNoteIds == null) return false
                for (id in selectedNoteIds) {
                    viewModel?.delete(id)
                }
                this.mode?.finish()
            }
            else -> this.mode?.finish()
        }

        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        this.mode = null
        selectionTracker?.clearSelection()
    }

    fun startActionMode(
        view: FragmentActivity,
        selectionTracker: SelectionTracker<Long>,
        noteViewModel: NoteViewModel,
        adapter: NoteAdapter,
        title: String?,
        onDeleteCallback: (Int) -> Unit
    ): ActionMode? {
        this.title = title
        this.selectionTracker = selectionTracker
        this.viewModel = noteViewModel
        this.adapter = adapter
        this.onDeleteCallback = onDeleteCallback
        return view.startActionMode(this)
    }
}