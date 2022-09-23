package com.zionhuang.music.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.ERROR_INFO
import com.zionhuang.music.constants.Constants.GITHUB_ISSUE_URL
import com.zionhuang.music.databinding.ActivityErrorBinding
import com.zionhuang.music.models.ErrorInfo
import com.zionhuang.music.ui.activities.base.ThemedBindingActivity

class ErrorActivity : ThemedBindingActivity<ActivityErrorBinding>() {
    override fun getViewBinding() = ActivityErrorBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val errorInfo = intent.getParcelableExtra<ErrorInfo>(ERROR_INFO)!!
        binding.stacktrace.text = errorInfo.stackTrace

        binding.btnCopy.setOnClickListener {
            val clipboardManager = getSystemService<ClipboardManager>()!!
            val clip = ClipData.newPlainText("stacktrace", errorInfo.stackTrace)
            clipboardManager.setPrimaryClip(clip)
            Toast.makeText(this, R.string.copied, LENGTH_SHORT).show()
        }
        binding.btnReport.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, GITHUB_ISSUE_URL.toUri()))
        }
    }

    companion object {
        fun openActivity(context: Context, errorInfo: ErrorInfo) {
            context.startActivity(getErrorActivityIntent(context, errorInfo))
        }

        private fun getErrorActivityIntent(context: Context, errorInfo: ErrorInfo) = Intent(context, ErrorActivity::class.java).apply {
            putExtra(ERROR_INFO, errorInfo)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}