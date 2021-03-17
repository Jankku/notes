package com.jankku.notes.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.noteId != "-1") {
            inflater.inflate(R.menu.menu_selection, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                val etNoteBody = binding.etNoteBody

                noteViewModel.delete(args.noteId.toLong())
                findNavController().navigateUp()
                etNoteBody.hideKeyboard()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        // Show keyboard and set selection to the end of text
        etNoteBody.showKeyboard()
        etNoteBody.setSelection(etNoteBody.length())

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

            when (noteId) {
                "-1" -> { // If note doesn't exist
                    etNoteBody.hideKeyboard()
                    noteViewModel.insert(Note(0, title, body, timeInMs, null))
                    findNavController().navigateUp()
                }
                else -> { // Update existing note
                    etNoteBody.hideKeyboard()
                    noteViewModel.partialUpdate(id, title, body, timeInMs)
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun EditText.showKeyboard() {
        post {
            if (this.requestFocus()) {
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun EditText.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }
}