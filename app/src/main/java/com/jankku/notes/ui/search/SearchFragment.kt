package com.jankku.notes.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.jankku.notes.NotesApplication
import com.jankku.notes.R
import com.jankku.notes.databinding.FragmentSearchBinding
import com.jankku.notes.ui.MainActivity
import com.jankku.notes.ui.common.SpaceItemDecoration
import com.jankku.notes.util.navigateSafe
import com.jankku.notes.util.showKeyboard
import com.jankku.notes.viewmodel.SearchViewModel
import com.jankku.notes.viewmodel.SearchViewModelFactory


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var application: Context? = null
    private var _adapter: NoteSearchAdapter? = null
    private val adapter get() = _adapter!!
    private var keyboardShownOnce = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        application = activity?.applicationContext
    }

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory((application as NotesApplication).noteDao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity).showSearchField()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchField = (requireActivity() as MainActivity).searchField
        setupRecyclerView()
        setupObservers()
        setupSearch(searchField)
    }

    override fun onDestroyView() {
        (requireActivity() as MainActivity).hideSearchField()
        super.onDestroyView()
        _adapter = null
        _binding = null
    }


    private fun setupRecyclerView() {
        _adapter = NoteSearchAdapter { note ->
            findNavController().navigateSafe(
                SearchFragmentDirections.actionSearchFragmentToDetailFragment(
                    getString(R.string.navigation_edit_note_label),
                    note
                )
            )
        }
        binding.rvSearch.apply {
            addItemDecoration(
                SpaceItemDecoration(
                    resources.getDimension(R.dimen.spacing_default).toInt()
                )
            )
        }
        binding.rvSearch.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.results.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    private fun setupSearch(searchView: SearchView) {
        if (!keyboardShownOnce) {
            keyboardShownOnce = true
            searchView.apply {
                isIconified = false
                showKeyboard()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean = true
        })
    }
}