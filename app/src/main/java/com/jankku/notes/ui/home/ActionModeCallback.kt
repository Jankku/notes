package com.jankku.notes.ui.home

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.selection.SelectionTracker
import com.jankku.notes.R
import com.jankku.notes.viewmodel.NoteViewModel

class ActionModeCallback(
    private val title: String,
    private val selectionTracker: SelectionTracker<Long>,
    private val viewModel: NoteViewModel,
    private val adapter: NoteAdapter,
    private val onDeleteCallback: (Int) -> Unit
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
        val pinnedList = selectedNoteIds.filter { selectedId ->
            adapter.currentList.toList().find { note -> note.id == selectedId }!!.pinned
        }

        if (selectedNoteIds.isEmpty()) return false

        when (item?.itemId) {
            R.id.action_pin -> {
                for (id in selectedNoteIds) {
                    val isPinned = pinnedList.contains(id)
                    viewModel.pin(id, !isPinned)
                }
                mode?.finish()
            }
            R.id.action_select_all -> {
                selectionTracker.setItemsSelected(noteIdList, true)
            }
            R.id.action_delete -> {
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
