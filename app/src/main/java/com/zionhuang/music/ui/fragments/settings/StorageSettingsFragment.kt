package com.zionhuang.music.ui.fragments.settings

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.format.Formatter
import androidx.core.net.toUri
import androidx.preference.NeoSeekBarPreference
import androidx.preference.Preference
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.zionhuang.music.R
import com.zionhuang.music.extensions.tryOrNull
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment

class StorageSettingsFragment : BaseSettingsFragment() {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
            if (iBinder !is MusicService.MusicBinder) return
            findPreference<NeoSeekBarPreference>(getString(R.string.pref_song_max_cache_size))?.apply {
                MediaSessionConnection.binder?.cache?.let { cache ->
                    tryOrNull { cache.cacheSpace }?.let { used ->
                        summary = getString(R.string.size_used, Formatter.formatShortFileSize(context, used))
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_storage)

        findPreference<Preference>(getString(R.string.pref_open_saf))?.setOnPreferenceClickListener {
            try {
                startActivity(Intent(ACTION_VIEW, "content://${requireContext().packageName}.provider/root/root".toUri()).apply {
                    setPackage("com.google.android.documentsui")
                    setClassName("com.google.android.documentsui", "com.android.documentsui.files.FilesActivity")
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            true
        }
        val maxImageCacheSizePreference = findPreference<NeoSeekBarPreference>(getString(R.string.pref_image_max_cache_size))!!.apply {
            setLabelFormatter {
                VALUE_TO_SIZE_TEXT[it]
            }
            context.imageLoader.diskCache?.let { diskCache ->
                summary = getString(R.string.size_used, Formatter.formatShortFileSize(context, diskCache.size))
            }
        }
        findPreference<Preference>(getString(R.string.pref_clear_image_cache))?.apply {
            context.imageLoader.diskCache?.let { diskCache ->
                setOnPreferenceClickListener {
                    diskCache.clear()
                    maxImageCacheSizePreference.summary = getString(R.string.size_used, Formatter.formatShortFileSize(context, diskCache.size))
                    true
                }
            }

        }
        findPreference<NeoSeekBarPreference>(getString(R.string.pref_song_max_cache_size))?.apply {
            setLabelFormatter {
                VALUE_TO_SIZE_TEXT[it]
            }
        }

        requireContext().bindService(Intent(requireContext(), MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        requireContext().unbindService(serviceConnection)
        super.onDestroy()
    }

    companion object {
        val VALUE_TO_SIZE_TEXT = listOf("128MB", "256MB", "512MB", "1GB", "2GB", "4GB", "8GB", "∞")
        val VALUE_TO_MB = listOf(128, 256, 512, 1024, 2048, 4096, 8192, -1)
    }
}