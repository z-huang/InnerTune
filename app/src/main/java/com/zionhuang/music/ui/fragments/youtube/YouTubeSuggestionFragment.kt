package com.zionhuang.music.ui.fragments.youtube

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_PREVIOUS
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentYoutubeSuggestionBinding
import com.zionhuang.music.extensions.getTextChangeFlow
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.extensions.systemBarInsetsCompat
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.adapters.YouTubeItemAdapter
import com.zionhuang.music.ui.fragments.base.NavigationFragment
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

    private val args: YouTubeSuggestionFragmentArgs by navArgs()

    private val viewModel by viewModels<SuggestionViewModel>()
    private val adapter = YouTubeItemAdapter(NavigationEndpointHandler(this))

    private val voiceResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val spokenText = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (spokenText != null) {
                binding.searchView.setText(spokenText)
            }
        }
    }

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
            setOnApplyWindowInsetsListener { v, insets ->
                v.updatePadding(bottom = insets.systemBarInsetsCompat.bottom)
                insets
            }
        }
        binding.btnVoice.setOnClickListener {
            try {
                voiceResultLauncher.launch(Intent(ACTION_RECOGNIZE_SPEECH))
            } catch (_: ActivityNotFoundException) {
            }
        }
        setupSearchView()
        showKeyboard()
        args.query?.let { query ->
            binding.searchView.setText(query)
            binding.searchView.setSelection(query.length)
        }
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
                    binding.btnClear.isVisible = it.isNotEmpty()
                }
        }
        binding.searchView.setOnEditorActionListener { view, actionId, event ->
            if (actionId == IME_ACTION_PREVIOUS) {
                hideKeyboard()
                true
            } else if ((event?.keyCode == KEYCODE_ENTER && event.action == ACTION_DOWN) || event?.action == IME_ACTION_SEARCH) {
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
        if (!sharedPreferences.getBoolean(getString(R.string.pref_pause_search_history), false)) {
            GlobalScope.launch {
                SongRepository(requireContext()).insertSearchHistory(query)
            }
        }
        exitTransition = null
        val action = YouTubeSuggestionFragmentDirections.actionSearchSuggestionToSearchResult(query)
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun showKeyboard() = showKeyboard(requireActivity(), binding.searchView)
    private fun hideKeyboard() = hideKeyboard(requireActivity(), binding.searchView)
}