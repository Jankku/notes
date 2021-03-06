package com.jankku.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.*
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
import java.util.*

class AddNoteFragment : Fragment() {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val args: AddNoteFragmentArgs by navArgs()
    private var application: Context? = null
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
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences()
        setupTextFields()
        setupInfoFields()
        setupSaveNoteOnBackPress()
        setupSaveFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupTextFields() {
        binding.etNoteTitle.setText(args.noteTitle, TextView.BufferType.EDITABLE)
        binding.etNoteBody.setText(args.noteBody, TextView.BufferType.EDITABLE)

        binding.etNoteTitle.addTextChangedListener {
            noteEdited = true
        }

        binding.etNoteBody.addTextChangedListener {
            noteEdited = true
        }
    }

    private fun preferences() {
        val keyboardPref = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getBoolean(getString(R.string.hide_keyboard_key), false)

        // Show keyboard if hide keyboard setting is false
        if (!keyboardPref) {
            binding.etNoteBody.showKeyboard()
            binding.etNoteBody.setSelection(binding.etNoteBody.length())
        }

        val saveFabPref = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getBoolean(getString(R.string.show_save_fab_key), false)

        // Show save FAB if the preference is true
        if (saveFabPref) {
            binding.fabSave.visibility = View.VISIBLE
        }
    }

    private fun setupInfoFields() {
        when (args.createdOn) {
            "" -> binding.tvCreatedOn.visibility = View.GONE
            else -> {
                val createdDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(args.createdOn.toLong())
                    .toString()
                binding.tvCreatedOn.text = getString(R.string.createdOn, createdDate)
            }
        }

        when (args.editedOn) {
            "" -> binding.tvEditedOn.visibility = View.GONE
            "null" -> binding.tvEditedOn.visibility = View.GONE
            else -> {
                val editedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(args.editedOn.toLong())
                    .toString()
                binding.tvEditedOn.text = getString(R.string.editedOn, editedDate)
            }
        }
    }

    private fun setupSaveNoteOnBackPress() {
        // Save/update note on back press if the note isn't empty
        // https://developer.android.com/guide/navigation/navigation-custom-back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            saveOrUpdateNote(
                args.noteId,
                binding.etNoteTitle.text.toString(),
                binding.etNoteBody.text.toString(),
                Calendar.getInstance().timeInMillis
            )
        }
    }

    private fun setupSaveFab() {
        binding.fabSave.setOnClickListener {
            binding.etNoteBody.hideKeyboard()
            saveOrUpdateNote(
                args.noteId,
                binding.etNoteTitle.text.toString(),
                binding.etNoteBody.text.toString(),
                Calendar.getInstance().timeInMillis
            )
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

    private fun deleteNote(noteId: String) {
        lifecycleScope.launch {
            noteViewModel.delete(noteId.toLong())
        }

        findNavController().navigateUp()

        Snackbar.make(
            binding.etNoteBody,
            R.string.snackbar_note_deleted,
            Snackbar.LENGTH_LONG
        ).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // If note exists, inflate menu
        if (args.noteId != "-1") {
            inflater.inflate(R.menu.menu_note, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                binding.etNoteBody.hideKeyboard()
                deleteNote(args.noteId)

                true
            }
            android.R.id.home -> {
                binding.etNoteBody.hideKeyboard()

                saveOrUpdateNote(
                    args.noteId,
                    binding.etNoteTitle.text.toString(),
                    binding.etNoteBody.text.toString(),
                    Calendar.getInstance().timeInMillis
                )

                true
            }
            else -> {
                binding.etNoteBody.hideKeyboard()
                super.onOptionsItemSelected(item)
            }
        }
    }
}