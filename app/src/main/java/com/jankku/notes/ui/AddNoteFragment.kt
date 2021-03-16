package com.jankku.notes.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentAddNoteBinding
import com.jankku.notes.db.Note
import com.jankku.notes.db.NoteViewModel
import com.jankku.notes.db.NoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private var application: Context? = null
    private val args: AddNoteFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = activity?.applicationContext
    }

    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Arguments
        val noteId = args.noteId
        val noteTitle = args.noteTitle
        val noteBody = args.noteBody
        val createdOn = args.createdOn
        val editedOn = args.editedOn

        // Views
        val etNoteTitle = binding.etNoteTitle
        val etNoteBody = binding.etNoteBody
        val tvCreatedOn = binding.tvCreatedOn
        val tvEditedOn = binding.tvEditedOn

        etNoteTitle.setText(noteTitle, TextView.BufferType.EDITABLE)
        etNoteBody.setText(noteBody, TextView.BufferType.EDITABLE)

        when (createdOn) {
            "" -> tvCreatedOn.visibility = GONE
            else -> {
                val createdDate = SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(createdOn.toLong())
                    .toString()
                tvCreatedOn.text = getString(R.string.createdOn, createdDate)
            }
        }

        when (editedOn) {
            "" -> tvEditedOn.visibility = GONE
            "null" -> tvEditedOn.visibility = GONE
            else -> {
                val editedDate = SimpleDateFormat("dd/MM/yyyy HH:mm")
                    .format(editedOn.toLong())
                    .toString()
                tvEditedOn.text = getString(R.string.editedOn, editedDate)
            }
        }


        val fab = binding.fabSave
        fab.setOnClickListener {
            val id = noteId.toLong()
            val title = etNoteTitle.text.toString()
            val body = etNoteBody.text.toString()
            val timeInMs = Calendar.getInstance().timeInMillis

            if (title.isEmpty() || body.isEmpty()) {
                Toast.makeText(application, R.string.empty_note, Toast.LENGTH_SHORT).show()
            } else {
                when (noteId) {
                    "-1" -> { // If note doesn't exist
                        activity?.hideSoftKeyboard()
                        noteViewModel.insert(Note(0, title, body, timeInMs, null))
                        findNavController().navigate(R.id.action_addNoteFragment_to_homeFragment)
                    }
                    else -> { // Update existing note
                        activity?.hideSoftKeyboard()
                        noteViewModel.partialUpdate(id, title, body, timeInMs)
                        findNavController().navigate(R.id.action_addNoteFragment_to_homeFragment)
                    }
                }
            }
        }
    }

    private fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager =
                ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}