package com.jankku.notes.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jankku.notes.databinding.NoteItemBinding
import com.jankku.notes.db.model.Note

class NoteSearchAdapter(
    private val clickListener: (Note) -> Unit,
) : ListAdapter<Note, NoteSearchAdapter.NoteViewHolder>(diffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NoteItemBinding.inflate(inflater, parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteSearchAdapter.NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun getItemViewType(position: Int): Int = getItem(position).hashCode()

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class NoteViewHolder(private val binding: NoteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var noteId: Long? = null

        fun bind(
            note: Note,
        ) = with(itemView) {
            binding.tvNoteTitle.text = note.title
            binding.tvNoteBody.text = note.getTruncatedBody()
            noteId = note.id

            itemView.setOnClickListener {
                clickListener(note)
            }
        }
    }
}
