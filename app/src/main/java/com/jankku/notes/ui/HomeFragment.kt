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
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.google.android.material.snackbar.Snackbar
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentHomeBinding
import com.jankku.notes.db.NoteViewModel
import com.jankku.notes.db.NoteViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var application: Context
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var actionMode: ActionMode? = null
    private var noteList: MutableList<Long> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = requireActivity().applicationContext
    }

    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        adapter = NoteAdapter(
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
                recyclerView,
                R.string.snackbar_note_deleted,
                Snackbar.LENGTH_LONG
            ).show()

            noteList.removeAt(position)
            adapter.notifyItemMoved(position + 1, position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(0, noteList.size)
            noteViewModel.delete(noteId)
        }

        recyclerView = binding.recyclerview

        val viewModePreference = PreferenceManager
            .getDefaultSharedPreferences(application)
            .getString(getString(R.string.view_mode_key), null)

        if (viewModePreference == "list") {
            recyclerView.layoutManager = object : LinearLayoutManager(application) {
                override fun supportsPredictiveItemAnimations(): Boolean = false
            }
        } else {
            recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        }

        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)

        // Swipe to delete action
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter, requireContext()))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Shrink FAB on scroll
        // https://stackoverflow.com/questions/32038332/using-google-design-library-how-to-hide-fab-button-on-scroll-down
        recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 10)
                        binding.fabAdd.shrink()
                    else if (dy < 0)
                        binding.fabAdd.extend()
                }
            })

        selectionTracker = SelectionTracker.Builder(
            "itemSelection",
            recyclerView,
            NoteKeyProvider(adapter),
            NoteDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        selectionTracker.onRestoreInstanceState(savedInstanceState)
        adapter.selectionTracker = selectionTracker

        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onItemStateChanged(key: Long, selected: Boolean) {
                    val selectedItems = selectionTracker.selection.size()
                    actionModeSelection(selectedItems)
                }
            })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        actionMode?.finish()
        actionMode = null
    }
}