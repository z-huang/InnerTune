package com.zionhuang.music.ui.fragments.youtube

import android.os.Bundle
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_PREVIOUS
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentYoutubeSuggestionBinding
import com.zionhuang.music.extensions.getTextChangeFlow
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.ui.fragments.base.NavigationFragment
import com.zionhuang.music.ui.fragments.youtube.YouTubeSuggestionFragmentDirections.actionSuggestionFragmentToSearchResultFragment
import com.zionhuang.music.utils.KeyboardUtil.hideKeyboard
import com.zionhuang.music.utils.KeyboardUtil.showKeyboard
import com.zionhuang.music.utils.NavigationEndpointHandler
import com.zionhuang.music.viewmodels.SuggestionViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class YouTubeSuggestionFragment : NavigationFragment<FragmentYoutubeSuggestionBinding>() {
    override fun getViewBinding() = FragmentYoutubeSuggestionBinding.inflate(layoutInflater)
    override fun getToolbar(): Toolbar = binding.toolbar

    private val viewModel by viewModels<SuggestionViewModel>()
    private val adapter = YouTubeItemAdapter(NavigationEndpointHandler(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).setDuration(resources.getInteger(R.integer.motion_duration_large).toLong())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.onFillQuery = { query ->
            binding.searchView.setText(query)
            binding.searchView.setSelection(query.length)
        }
        adapter.onSearch = this::search
        adapter.onRefreshSuggestions = {
            viewModel.fetchSuggestions(binding.searchView.text.toString())
        }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@YouTubeSuggestionFragment.adapter
        }
        setupSearchView()
        showKeyboard()
        viewModel.suggestions.observe(viewLifecycleOwner) { dataSet ->
            adapter.submitList(dataSet)
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchView() {
        lifecycleScope.launch {
            binding.searchView
                .getTextChangeFlow()
                .debounce(100L)
                .collectLatest {
                    viewModel.fetchSuggestions(it)
                }
        }
        binding.searchView.setOnEditorActionListener { view, actionId, event ->
            if (actionId == IME_ACTION_PREVIOUS) {
                hideKeyboard()
                true
            } else if (event?.keyCode == KEYCODE_ENTER || event?.action == IME_ACTION_SEARCH) {
                hideKeyboard()
                search(view.text.toString())
                true
            } else {
                false
            }
        }
        binding.btnClear.setOnClickListener {
            binding.searchView.text.clear()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun search(query: String) {
        GlobalScope.launch {
            SongRepository.insertSearchHistory(query)
        }
        exitTransition = null
        val action = actionSuggestionFragmentToSearchResultFragment(query)
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun showKeyboard() = showKeyboard(requireActivity(), binding.searchView)
    private fun hideKeyboard() = hideKeyboard(requireActivity(), binding.searchView)
}