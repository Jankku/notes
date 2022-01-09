package com.jankku.notes.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentHomeBinding
import com.jankku.notes.ui.MainActivity
import com.jankku.notes.util.ShrinkFabOnScroll
import com.jankku.notes.util.navigateSafe
import com.jankku.notes.util.showSnackBar
import com.jankku.notes.viewmodel.NoteViewModel
import com.jankku.notes.viewmodel.NoteViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: NoteAdapter? = null
    private val adapter get() = _adapter!!
    private var actionMode: ActionMode? = null
    private var _selectionTracker: SelectionTracker<Long>? = null
    private val selectionTracker get() = _selectionTracker!!
    private lateinit var application: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().applicationContext
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSelectionTracker()
        setupAddFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        actionMode = null
        _selectionTracker = null
        _adapter = null
        _binding = null
    }

    private fun setupObservers() {
        viewModel.allNotes.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.noNotes.clNoNotes.isVisible = list.isEmpty()
        }

        viewModel.noteCount.observe(viewLifecycleOwner) { count ->
            (requireActivity() as? MainActivity)?.setCustomTitle(
                getString(
                    R.string.navigation_home_label,
                    count
                )
            )
        }
    }

    private fun setupRecyclerView() {
        _adapter = NoteAdapter { note ->
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    note,
                    getString(R.string.navigation_edit_note_label)
                )
            )
        }

        val selectedViewMode = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getString(getString(R.string.view_mode_key), null)

        if (selectedViewMode == "list") {
            binding.recyclerview.layoutManager = object : LinearLayoutManager(application) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
        } else {
            binding.recyclerview.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        binding.recyclerview.adapter = adapter
    }


    private fun setupAddFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    title = getString(R.string.navigation_new_note_label)
                )
            )
        }

        binding.recyclerview.addOnScrollListener(ShrinkFabOnScroll(binding.fabAdd))
    }

    private fun setupSelectionTracker() {
        _selectionTracker = SelectionTracker.Builder(
            "itemSelection",
            binding.recyclerview,
            NoteKeyProvider(adapter),
            NoteDetailsLookup(binding.recyclerview),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        adapter.selectionTracker = selectionTracker

        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onItemStateChanged(key: Long, selected: Boolean) {
                    val selectedItems = selectionTracker.selection.size()
                    actionModeSelection(selectedItems)
                }
            })
    }

    private fun actionModeSelection(selectedItems: Int) {
        val title = selectionTracker.selection.size().toString()
        val actionModeCallback = ActionModeCallback(
            title,
            selectionTracker,
            viewModel,
            adapter,
            onDeleteCallback = { deleteCount ->
                val message =
                    if (deleteCount == 1) getString(R.string.snackbar_note_deleted)
                    else getString(R.string.snackbar_notes_deleted, deleteCount)
                showSnackBar(binding.root, message)
            })

        when {
            selectedItems == 0 -> {
                actionMode?.finish()
                actionMode = null
            }
            actionMode == null -> {
                actionMode = actionModeCallback.startActionMode(requireActivity())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigateSafe(R.id.action_homeFragment_to_settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_selectionTracker !== null) {
            selectionTracker.onSaveInstanceState(outState)
        }
    }
}
