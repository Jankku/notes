package com.jankku.notes.ui.home

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.SelectionTracker
import com.jankku.notes.R
import com.jankku.notes.viewmodel.NoteViewModel

class ActionModeCallback(
    val title: String,
    private val selectionTracker: SelectionTracker<Long>,
    private val viewModel: NoteViewModel,
    private val adapter: NoteAdapter,
    val onDeleteCallback: (Int) -> Unit
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        mode.menuInflater?.inflate(R.menu.menu_selection, menu)
        mode.title = title
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        val selectedNoteIds = this.selectionTracker.selection.toList()
        val noteIdList = adapter.currentList.toList().map { it.id }.asIterable()

        when (item?.itemId) {
            R.id.action_select_all -> {
                selectionTracker.setItemsSelected(noteIdList, true)
            }
            R.id.action_delete -> {
                if (selectedNoteIds.isEmpty()) return false

                for (id in selectedNoteIds) {
                    viewModel.delete(id)
                }
                onDeleteCallback(selectedNoteIds.size)

                mode?.finish()
            }
            else -> mode?.finish()
        }

        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectionTracker.clearSelection()
    }

    fun startActionMode(
        view: FragmentActivity,
    ): ActionMode? {
        return view.startActionMode(this)
    }
}