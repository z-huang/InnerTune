package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_LEGACY
import android.text.format.Formatter.formatShortFileSize
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.zionhuang.music.R
import com.zionhuang.music.databinding.FragmentUpdateBinding
import com.zionhuang.music.models.DownloadProgress
import com.zionhuang.music.ui.fragments.base.BindingFragment
import com.zionhuang.music.update.UpdateStatus
import com.zionhuang.music.utils.DownloadProgressLiveData
import com.zionhuang.music.viewmodels.UpdateViewModel
import kotlinx.coroutines.launch
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer


class UpdateFragment : BindingFragment<FragmentUpdateBinding>() {
    override fun getViewBinding() = FragmentUpdateBinding.inflate(layoutInflater)
    private val viewModel by activityViewModels<UpdateViewModel>()
    private var downloadProgress: LiveData<DownloadProgress>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.updateBtn.setOnClickListener {
            binding.progressBar.progress = 0
            viewModel.updateApp()
        }
        lifecycleScope.launch {
            val latestRelease = viewModel.getLatestRelease()
            binding.version.text = latestRelease.version.toString()
            val parser: Parser = Parser.builder().build()
            val document: Node = parser.parse(latestRelease.body)
            val renderer = HtmlRenderer.builder().build()
            val html = renderer.render(document)
            binding.changelog.text = Html.fromHtml(html, FROM_HTML_MODE_LEGACY)
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            binding.updateStatusBox.isVisible = status !is UpdateStatus.Idle
            binding.progressBar.isIndeterminate = status is UpdateStatus.Preparing || status is UpdateStatus.Verifying
            binding.updateBtn.isEnabled = status is UpdateStatus.Idle
            when (status) {
                UpdateStatus.Idle -> binding.updateStatus.text = ""
                UpdateStatus.Preparing -> binding.updateStatus.text = getString(R.string.update_status_preparing)
                is UpdateStatus.Downloading -> {
                    downloadProgress = DownloadProgressLiveData(requireContext(), status.downloadId)
                    downloadProgress?.observe(viewLifecycleOwner) {
                        binding.progressBar.setProgress(it.currentBytes * 100 / it.totalBytes, true)
                        binding.updateStatus.text = getString(
                            R.string.update_status_downloading,
                            formatShortFileSize(requireContext(), if (it.currentBytes == -1) 0 else it.currentBytes.toLong()),
                            formatShortFileSize(requireContext(), if (it.totalBytes == -1) 0 else it.totalBytes.toLong())
                        )
                    }
                }
                UpdateStatus.Verifying -> binding.updateStatus.text = getString(R.string.update_status_verifying)
            }
        }
    }
}