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
import com.jankku.notes.ui.common.SpaceItemDecoration
import com.jankku.notes.util.navigateSafe
import com.jankku.notes.util.showToast
import com.jankku.notes.viewmodel.NoteViewModel
import com.jankku.notes.viewmodel.NoteViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var _pinnedAdapter: NoteAdapter? = null
    private val pinnedAdapter get() = _pinnedAdapter!!
    private var _pinnedSelectionTracker: SelectionTracker<Long>? = null
    private val pinnedSelectionTracker get() = _pinnedSelectionTracker!!

    private var _unpinnedAdapter: NoteAdapter? = null
    private val unpinnedAdapter get() = _unpinnedAdapter!!
    private var _unpinnedSelectionTracker: SelectionTracker<Long>? = null
    private val unpinnedSelectionTracker get() = _unpinnedSelectionTracker!!

    private var actionMode: ActionMode? = null
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
        setupSelectionTracker()
        setupObservers()
        setupAddFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionMode?.finish()
        actionMode = null
        _unpinnedSelectionTracker = null
        _pinnedSelectionTracker = null
        _pinnedAdapter = null
        _unpinnedAdapter = null
        _binding = null
    }

    private fun setupObservers() {
        viewModel.pinnedNotes.observe(viewLifecycleOwner) { pinnedNotes ->
            pinnedAdapter.submitList(pinnedNotes)
            binding.rvPinnedNotes.isVisible = pinnedNotes.isNotEmpty()
            binding.tvPinnedTitle.isVisible = pinnedNotes.isNotEmpty()
        }

        viewModel.unpinnedNotes.observe(viewLifecycleOwner) { unpinnedNotes ->
            unpinnedAdapter.submitList(unpinnedNotes)
            binding.rvUnpinnedNotes.isVisible = unpinnedNotes.isNotEmpty()

            viewModel.pinnedNotes.observe(viewLifecycleOwner) { pinnedNotes ->
                binding.tvUnpinnedTitle.isVisible =
                    unpinnedNotes.isNotEmpty() && pinnedNotes.isNotEmpty()
            }
        }

        viewModel.noteCount.observe(viewLifecycleOwner) { count ->
            binding.noNotes.clNoNotes.isVisible = count == 0
            (requireActivity() as? MainActivity)?.setCustomTitle(
                getString(
                    R.string.navigation_home_label,
                    count
                )
            )
        }
    }

    private fun setupRecyclerView() {
        _pinnedAdapter = NoteAdapter { note ->
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    getString(R.string.navigation_edit_note_label),
                    note
                )
            )
        }
        _unpinnedAdapter = NoteAdapter { note ->
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    getString(R.string.navigation_edit_note_label),
                    note
                )
            )
        }

        val selectedViewMode = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getString(getString(R.string.view_mode_key), null)
        when (selectedViewMode) {
            getString(R.string.view_mode_value_list) -> listView()
            getString(R.string.view_mode_value_grid) -> gridView()
            else -> listView()
        }

        binding.rvPinnedNotes.adapter = pinnedAdapter
        binding.rvUnpinnedNotes.adapter = unpinnedAdapter
    }


    private fun setupAddFab() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigateSafe(
                HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                    title = getString(R.string.navigation_new_note_label)
                )
            )
        }

        binding.nsvNotes.setOnScrollChangeListener { _, _, newY, _, oldY ->
            if (newY > oldY) {
                binding.fabAdd.shrink()
            } else {
                binding.fabAdd.extend()
            }
        }
    }

    private fun setupSelectionTracker() {
        _pinnedSelectionTracker = SelectionTracker.Builder(
            "pinnedSelection",
            binding.rvPinnedNotes,
            NoteKeyProvider(pinnedAdapter),
            NoteDetailsLookup(binding.rvPinnedNotes),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        _unpinnedSelectionTracker = SelectionTracker.Builder(
            "unpinnedSelection",
            binding.rvUnpinnedNotes,
            NoteKeyProvider(unpinnedAdapter),
            NoteDetailsLookup(binding.rvUnpinnedNotes),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()

        pinnedAdapter.selectionTracker = pinnedSelectionTracker
        unpinnedAdapter.selectionTracker = unpinnedSelectionTracker

        pinnedSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                val selectionSize = pinnedSelectionTracker.selection.size()
                val actionModeCallback = ActionModeCallback(
                    selectionSize.toString(),
                    pinnedSelectionTracker,
                    viewModel,
                    pinnedAdapter,
                    onDeleteCallback = { deleteCount ->
                        val message =
                            if (deleteCount == 1) getString(R.string.snackbar_note_deleted)
                            else getString(R.string.snackbar_notes_deleted, deleteCount)
                        requireContext().showToast(message)
                    })

                when {
                    selectionSize == 0 -> {
                        actionMode?.finish()
                        actionMode = null
                    }
                    actionMode == null -> {
                        actionMode = actionModeCallback.startActionMode(requireActivity())
                    }
                }
            }
        })

        unpinnedSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                val selectionSize = unpinnedSelectionTracker.selection.size()
                val actionModeCallback = ActionModeCallback(
                    selectionSize.toString(),
                    unpinnedSelectionTracker,
                    viewModel,
                    unpinnedAdapter,
                    onDeleteCallback = { deleteCount ->
                        val message =
                            if (deleteCount == 1) getString(R.string.snackbar_note_deleted)
                            else getString(R.string.snackbar_notes_deleted, deleteCount)
                        requireContext().showToast(message)
                    })

                when {
                    selectionSize == 0 -> {
                        actionMode?.finish()
                        actionMode = null
                    }
                    actionMode == null -> {
                        actionMode = actionModeCallback.startActionMode(requireActivity())
                    }
                }
            }
        })
    }

    private fun listView() {
        binding.rvPinnedNotes.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_default).toInt()
                )
            )
        }
        binding.rvUnpinnedNotes.apply {
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_default).toInt()
                )
            )
        }
    }

    private fun gridView() {
        binding.rvPinnedNotes.apply {
            layoutManager = StaggeredGridLayoutManager(
                resources.getInteger(R.integer.rv_grid_columns),
                StaggeredGridLayoutManager.VERTICAL
            )
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_default).toInt()
                )
            )
        }
        binding.rvUnpinnedNotes.apply {
            layoutManager = StaggeredGridLayoutManager(
                resources.getInteger(R.integer.rv_grid_columns),
                StaggeredGridLayoutManager.VERTICAL
            )
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_default).toInt()
                )
            )
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
            R.id.action_search -> {
                findNavController().navigateSafe(R.id.action_homeFragment_to_searchFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (_unpinnedSelectionTracker !== null) {
            unpinnedSelectionTracker.onSaveInstanceState(outState)
        }
        if (_pinnedSelectionTracker !== null) {
            pinnedSelectionTracker.onSaveInstanceState(outState)
        }
    }
}
