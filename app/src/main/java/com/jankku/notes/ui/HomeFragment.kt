package com.jankku.notes.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
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
    private lateinit var application: Context
    private lateinit var selectionTracker: SelectionTracker<Long>

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

        val adapter = NoteAdapter { note ->
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
            NoteAdapter.KeyProvider(adapter),
            NoteAdapter.DetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        selectionTracker.onRestoreInstanceState(savedInstanceState)
        adapter.selectionTracker = selectionTracker

        selectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val actionModeCallback = ActionModeCallback()
                val selectedItems = selectionTracker.selection

                actionModeCallback.startActionMode(
                    view,
                    application,
                    selectionTracker,
                    noteViewModel,
                    adapter,
                    "${selectedItems.size()}"
                )
            }

            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                if (selectionTracker.selection.size() == 0) {
                    selectionTracker.clearSelection()
                }
            }
        })

        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}