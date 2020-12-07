package com.zionhuang.music.ui.fragments

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentSearchSuggestionBinding
import com.zionhuang.music.extensions.addOnClickListener
import com.zionhuang.music.extensions.getQueryTextChangeFlow
import com.zionhuang.music.ui.adapters.SearchSuggestionAdapter
import com.zionhuang.music.ui.fragments.base.MainFragment
import com.zionhuang.music.viewmodels.SuggestionViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class SuggestionFragment : MainFragment<FragmentSearchSuggestionBinding>() {
    companion object {
        private const val TAG = "SearchSuggestionFragment"
    }

    private val viewModel by viewModels<SuggestionViewModel>()
    private lateinit var searchView: SearchView

    @FlowPreview
    @ExperimentalTime
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val suggestionAdapter = SearchSuggestionAdapter(viewModel)
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = suggestionAdapter
            addOnClickListener { pos, _ ->
                search(suggestionAdapter.getQueryByPosition(pos))
            }
        }
        setupSearchView()
        viewModel.apply {
            onFillQuery.observe(viewLifecycleOwner, { query -> searchView.setQuery(query, false) })
            query.observe(viewLifecycleOwner, { query -> viewModel.fetchSuggestions(query) })
            suggestions.observe(viewLifecycleOwner, { dataSet -> suggestionAdapter.setDataSet(dataSet) })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_view, menu)
        menu.findItem(R.id.search_view).actionView = searchView
    }

    @FlowPreview
    @ExperimentalTime
    private fun setupSearchView() {
        val searchManager = requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = SearchView(requireContext()).apply {
            isIconified = false
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text)?.setPadding(0, 2, 0, 2)
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isSubmitButtonEnabled = false
            maxWidth = Int.MAX_VALUE
            setOnCloseListener { true }
            viewLifecycleOwner.lifecycleScope.launch {
                getQueryTextChangeFlow()
                        .debounce(100.toDuration(TimeUnit.MILLISECONDS))
                        .collect { e ->
                            if (e.isSubmitted) {
                                search(e.query.orEmpty())
                            } else {
                                viewModel.setQuery(e.query)
                            }
                        }
            }
            setQuery(viewModel.query.value, false)
        }
    }

    private fun search(query: String) {
        searchView.clearFocus()
        val action = SuggestionFragmentDirections.actionSuggestionFragmentToSearchResultFragment(query)
        NavHostFragment.findNavController(this).navigate(action)
    }
}