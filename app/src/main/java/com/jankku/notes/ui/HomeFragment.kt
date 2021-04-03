package com.jankku.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
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

        adapter = NoteAdapter { note, position ->
            // RecyclerView item onclick action
            val action = HomeFragmentDirections.actionHomeFragmentToAddNoteFragment(
                noteId = note.id.toString(),
                noteTitle = note.title,
                noteBody = note.body,
                createdOn = note.createdOn.toString(),
                editedOn = note.editedOn.toString()
            )
            findNavController().navigate(action)
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

        // Shrink FAB on scroll
        // https://stackoverflow.com/questions/32038332/using-google-design-library-how-to-hide-fab-button-on-scroll-down
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10)
                    binding.fabAdd.shrink()
                else if (dy < 0)
                    binding.fabAdd.extend()
            }
        })

        noteViewModel.allNotes.observe(viewLifecycleOwner) { list ->
            list.let { adapter.submitList(it) }
            for (note in list) {
                noteList.add(note.id)
            }
            // Show the "No Notes" layout if the list is empty, otherwise keep it hidden
            if (list.isEmpty()) {
                binding.noNotes.clNoNotes.visibility = VISIBLE
            } else {
                binding.noNotes.clNoNotes.visibility = GONE
            }
        }

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

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
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

    class ActionModeCallback : ActionMode.Callback {
        private var mode: ActionMode? = null
        private var title: String? = null
        private var selectionTracker: SelectionTracker<Long>? = null
        private var noteViewModel: NoteViewModel? = null
        private var adapter: NoteAdapter? = null
        private var noteList: MutableList<Long>? = null

        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            this.mode = mode
            mode.menuInflater?.inflate(R.menu.menu_selection, menu)
            mode.title = title
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val noteIdList = this.selectionTracker!!.selection.toList()
            when (item?.itemId) {
                R.id.action_select_all -> {
                    selectionTracker!!.setItemsSelected(noteList!!.asIterable(), true)
                }
                R.id.action_delete -> {
                    Thread {
                        for (id in noteIdList) {
                            noteViewModel?.delete(id)
                        }
                    }.start()
                    this.mode?.finish()
                }
                else -> this.mode?.finish()
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            this.mode = null
        }

        fun startActionMode(
            view: FragmentActivity,
            selectionTracker: SelectionTracker<Long>,
            noteViewModel: NoteViewModel,
            adapter: NoteAdapter,
            noteList: MutableList<Long>,
            title: String?
        ): ActionMode? {
            this.title = title
            this.selectionTracker = selectionTracker
            this.noteViewModel = noteViewModel
            this.adapter = adapter
            this.noteList = noteList
            return view.startActionMode(this)
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