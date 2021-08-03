package com.jankku.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentAddNoteBinding
import com.jankku.notes.db.Note
import com.jankku.notes.util.Keyboard.Companion.hideKeyboard
import com.jankku.notes.util.Keyboard.Companion.showKeyboard
import com.jankku.notes.viewmodel.NoteViewModel
import com.jankku.notes.viewmodel.NoteViewModelFactory
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.DateFormat.SHORT
import java.util.*

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private var application: Context? = null
    private val args: AddNoteFragmentArgs by navArgs()
    private var noteEdited: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = activity?.applicationContext
    }

    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).noteDao)
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

        etNoteTitle.addTextChangedListener {
            noteEdited = true
        }

        etNoteBody.addTextChangedListener {
            noteEdited = true
        }

        val keyboardPref = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getBoolean(getString(R.string.hide_keyboard_key), false)

        // Show keyboard if hide keyboard setting is false
        if (!keyboardPref) {
            etNoteBody.showKeyboard()
            etNoteBody.setSelection(etNoteBody.length())
        }

        when (createdOn) {
            "" -> tvCreatedOn.visibility = GONE
            else -> {
                val createdDate = DateFormat.getDateTimeInstance(SHORT, SHORT)
                    .format(createdOn.toLong())
                    .toString()
                tvCreatedOn.text = getString(R.string.createdOn, createdDate)
            }
        }

        when (editedOn) {
            "" -> tvEditedOn.visibility = GONE
            "null" -> tvEditedOn.visibility = GONE
            else -> {
                val editedDate = DateFormat.getDateTimeInstance(SHORT, SHORT)
                    .format(editedOn.toLong())
                    .toString()
                tvEditedOn.text = getString(R.string.editedOn, editedDate)
            }
        }

        // Save/update note on back press if the note isn't empty
        // https://developer.android.com/guide/navigation/navigation-custom-back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val title = etNoteTitle.text.toString()
            val body = etNoteBody.text.toString()
            val timeInMs = Calendar.getInstance().timeInMillis

            saveOrUpdateNote(noteId, title, body, timeInMs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // If note exists, inflate menu
        if (args.noteId != "-1") {
            inflater.inflate(R.menu.menu_note, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val etNoteBody = binding.etNoteBody

        return when (item.itemId) {
            R.id.action_delete -> {
                etNoteBody.hideKeyboard()

                lifecycleScope.launch {
                    noteViewModel.delete(args.noteId.toLong())
                }

                findNavController().navigateUp()

                Snackbar.make(
                    binding.etNoteBody,
                    R.string.snackbar_note_deleted,
                    Snackbar.LENGTH_LONG
                ).show()

                true
            }
            android.R.id.home -> {
                val id = args.noteId
                val title = binding.etNoteTitle.text.toString()
                val body = binding.etNoteBody.text.toString()
                val timeInMs = Calendar.getInstance().timeInMillis

                etNoteBody.hideKeyboard()

                saveOrUpdateNote(id, title, body, timeInMs)

                true
            }
            else -> {
                etNoteBody.hideKeyboard()
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun saveOrUpdateNote(
        noteId: String,
        title: String,
        body: String,
        timeInMs: Long
    ) {
        if (title.isEmpty() && body.isEmpty()) {
            findNavController().navigateUp()
            return
        }

        if (!noteEdited) {
            findNavController().navigateUp()
            return
        }

        lifecycleScope.launch {
            val id = noteId.toLong()
            when (noteId) {
                "-1" -> noteViewModel.insert(Note(0, title, body, timeInMs, null))
                else -> noteViewModel.update(id, title, body, timeInMs)
            }
        }

        findNavController().navigateUp()
    }
}