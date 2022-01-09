package com.jankku.notes.ui.detail

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentAddNoteBinding
import com.jankku.notes.util.Event
import com.jankku.notes.util.hideKeyboard
import com.jankku.notes.util.showKeyboard
import com.jankku.notes.util.showSnackBar
import com.jankku.notes.viewmodel.NoteViewModel
import com.jankku.notes.viewmodel.NoteViewModelFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DateFormat

class DetailFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val args: DetailFragmentArgs by navArgs()
    private var application: Context? = null
    private lateinit var prefs: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = activity?.applicationContext
        prefs = PreferenceManager.getDefaultSharedPreferences(application)
    }

    private val viewModel: NoteViewModel by viewModels {
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
        setupEventListener()
        setupTextFields()
        setupInfoFields()
        setupSaveNoteOnBackPress()
        setupSaveFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupEventListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventChannel
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .collect { event ->
                    when (event) {
                        is Event.NavigateUp -> findNavController().navigateUp()
                    }
                }
        }
    }

    private fun setupTextFields() {
        val hideKeyboardPref = prefs.getBoolean(getString(R.string.hide_keyboard_key), false)

        binding.etNoteTitle.apply {
            setText(args.note?.title, TextView.BufferType.EDITABLE)
            addTextChangedListener {
                viewModel.noteEdited.value = true
            }
        }

        binding.etNoteBody.apply {
            setText(args.note?.body, TextView.BufferType.EDITABLE)
            if (!hideKeyboardPref) {
                setSelection(binding.etNoteBody.text.length)
                requestFocus()
                showKeyboard()
            }
            addTextChangedListener {
                viewModel.noteEdited.value = true
            }
        }
    }

    private fun setupInfoFields() {
        when (args.note?.createdOn) {
            is Long -> {
                val createdDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(args.note?.createdOn)
                    .toString()
                binding.tvCreatedOn.text = getString(R.string.createdOn, createdDate)

            }
            else -> binding.tvCreatedOn.visibility = View.GONE
        }

        when (args.note?.editedOn.toString()) {
            "" -> binding.tvEditedOn.visibility = View.GONE
            "null" -> binding.tvEditedOn.visibility = View.GONE
            else -> {
                val editedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(args.note?.editedOn)
                    .toString()
                binding.tvEditedOn.text = getString(R.string.editedOn, editedDate)
            }
        }
    }

    private fun setupSaveNoteOnBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.insertOrUpdate(
                args.note?.id,
                binding.etNoteTitle.text.toString(),
                binding.etNoteBody.text.toString(),
                System.currentTimeMillis()
            )
        }
    }

    private fun setupSaveFab() {
        val showSaveFabPref = prefs.getBoolean(getString(R.string.show_save_fab_key), false)
        if (showSaveFabPref) {
            binding.fabSave.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    binding.etNoteBody.hideKeyboard()
                    viewModel.insertOrUpdate(
                        args.note?.id,
                        binding.etNoteTitle.text.toString(),
                        binding.etNoteBody.text.toString(),
                        System.currentTimeMillis()
                    )
                }
            }
        }

    }

    private fun deleteNote(noteId: Long?) {
        if (noteId == null) return
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.delete(noteId)
        }
        showSnackBar(binding.root, getString(R.string.snackbar_note_deleted))
        viewModel.sendEvent(Event.NavigateUp)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (args.note?.id == null) return
        inflater.inflate(R.menu.menu_note, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.etNoteBody.hideKeyboard()
        return when (item.itemId) {
            R.id.action_delete -> {
                deleteNote(args.note?.id)
                true
            }
            android.R.id.home -> {
                viewModel.insertOrUpdate(
                    args.note?.id,
                    binding.etNoteTitle.text.toString(),
                    binding.etNoteBody.text.toString(),
                    System.currentTimeMillis()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}