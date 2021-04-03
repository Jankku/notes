package com.jankku.notes.ui

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jankku.notes.R
import com.jankku.notes.db.Note


class NoteAdapter internal constructor(
    private val clickListener: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NOTES_COMPARATOR) {

    lateinit var selectionTracker: SelectionTracker<Long>

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val noteTitle: TextView = itemView.findViewById(R.id.tvNoteTitle)
        private val noteBody: TextView = itemView.findViewById(R.id.tvNoteBody)
        private var noteId: Long? = null

        fun bind(
            note: Note,
        ) = with(itemView) {
            noteTitle.text = note.title
            noteBody.text = note.body
            noteId = note.id
            itemView.setOnClickListener { clickListener(note) }
            bindSelectedState(
                this,
                selectionTracker.isSelected(getItem(adapterPosition).id)
            )
        }

        private fun bindSelectedState(view: View, selected: Boolean) {
            view.isActivated = selected
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = getItem(adapterPosition).id
                override fun inSelectionHotspot(e: MotionEvent): Boolean = false
                override fun inDragRegion(e: MotionEvent): Boolean = true
            }
    }

    class KeyProvider(private val adapter: NoteAdapter) :
        ItemKeyProvider<Long>(SCOPE_MAPPED) {

        override fun getKey(position: Int): Long {
            return adapter.currentList[position].id
        }

        override fun getPosition(key: Long): Int {
            return adapter.currentList.indexOfFirst { it.id == key }
        }

    }

    class DetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as NoteViewHolder).getItemDetails()
            }
            return null
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = position

    companion object {
        private val NOTES_COMPARATOR = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }
}
