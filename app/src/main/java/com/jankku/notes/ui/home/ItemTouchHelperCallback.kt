package com.jankku.notes.ui.home

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView

// https://medium.com/@zackcosborn/step-by-step-recyclerview-swipe-to-delete-and-undo-7bbae1fce27e
// https://www.geeksforgeeks.org/swipe-to-delete-and-undo-in-android-recyclerview/
// https://nayan.co/blog/Android/android-recycler-view-drag-and-drop/
class ItemTouchHelperCallback : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val adapter = recyclerView.adapter as NoteAdapter
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition
        adapter.notifyItemMoved(fromPosition, toPosition)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}
