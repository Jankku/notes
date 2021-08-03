package com.jankku.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentHomeBinding
import com.jankku.notes.viewmodel.NoteViewModel
import com.jankku.notes.viewmodel.NoteViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var _adapter: NoteAdapter? = null
    private val adapter get() = _adapter!!
    private var actionMode: ActionMode? = null
    private lateinit var application: Context
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var noteList: MutableList<Long> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().applicationContext
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSelectionTracker()

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _adapter = null
        actionMode?.finish()
        actionMode = null
    }

    private fun setupObservers() {
        noteViewModel.allNotes.observe(viewLifecycleOwner) { list ->
            list.let {
                adapter.submitList(it)
                noteList.clear()
                for (note in it) {
                    noteList.add(note.id)
                }
            }
            // Show the "No Notes" layout if the list is empty, otherwise keep it hidden
            if (list.isEmpty()) {
                binding.noNotes.clNoNotes.visibility = View.VISIBLE
            } else {
                binding.noNotes.clNoNotes.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        _adapter = NoteAdapter(
            { note -> // Click listener
                val action = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(
                    noteId = note.id.toString(),
                    noteTitle = note.title,
                    noteBody = note.body,
                    createdOn = note.createdOn.toString(),
                    editedOn = note.editedOn.toString()
                )
                findNavController().navigate(action)
            }
        ) { position -> // Swipe listener
            val noteId = noteList[position]

            Snackbar.make(
                binding.recyclerview,
                R.string.snackbar_note_deleted,
                Snackbar.LENGTH_LONG
            ).show()

            noteList.removeAt(position)
            adapter.notifyItemMoved(position + 1, position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(0, noteList.size)
            noteViewModel.delete(noteId)
        }

        val viewModePreference = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getString(getString(R.string.view_mode_key), null)

        if (viewModePreference == "list") {
            binding.recyclerview.layoutManager = object : LinearLayoutManager(application) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
        } else {
            binding.recyclerview.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter = adapter

        // Swipe to delete action
        val itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(adapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(binding.recyclerview)

        // Shrink FAB on scroll
        // https://stackoverflow.com/questions/32038332/using-google-design-library-how-to-hide-fab-button-on-scroll-down
        binding.recyclerview.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 10)
                        binding.fabAdd.shrink()
                    else if (dy < 0)
                        binding.fabAdd.extend()
                }
            })
    }

    private fun setupSelectionTracker() {
        selectionTracker = SelectionTracker.Builder(
            "itemSelection",
            binding.recyclerview,
            NoteKeyProvider(adapter),
            NoteDetailsLookup(binding.recyclerview),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

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
        val actionModeCallback = ActionModeCallback()
        actionMode =
            if (selectedItems == 0) {
                actionMode?.finish()
                null
            } else {
                actionModeCallback.startActionMode(
                    requireActivity(),
                    selectionTracker,
                    noteViewModel,
                    adapter,
                    noteList,
                    selectionTracker.selection.size().toString()
                )
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Can be called before onCreateView
        if (::selectionTracker.isInitialized) {
            selectionTracker.onSaveInstanceState(outState)
        }
    }
}