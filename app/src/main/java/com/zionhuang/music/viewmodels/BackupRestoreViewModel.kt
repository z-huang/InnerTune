package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.music.App
import com.zionhuang.music.R
import com.zionhuang.music.db.InternalDatabase
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.extensions.zipInputStream
import com.zionhuang.music.extensions.zipOutputStream
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.playback.SongPlayer
import com.zionhuang.music.ui.screens.settings.PREF_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    application: Application,
    val database: MusicDatabase,
) : AndroidViewModel(application) {
    val app = getApplication<App>()
    fun backup(uri: Uri) {
        runCatching {
            app.applicationContext.contentResolver.openOutputStream(uri)?.use {
                it.buffered().zipOutputStream().use { outputStream ->
                    File(
                        File(app.filesDir.parentFile, "shared_prefs"),
                        "${app.packageName}_preferences.xml"
                    ).inputStream().buffered().use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(PREF_NAME))
                        inputStream.copyTo(outputStream)
                    }
                    database.checkpoint()
                    FileInputStream(database.delegate.openHelper.writableDatabase.path).use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(InternalDatabase.DB_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }.onSuccess {
            Toast.makeText(app, R.string.message_backup_create_success, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(app, R.string.message_backup_create_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun restore(uri: Uri) {
        runCatching {
            this.app.applicationContext.contentResolver.openInputStream(uri)?.use {
                it.zipInputStream().use { inputStream ->
                    var entry = inputStream.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            PREF_NAME -> {
                                File(
                                    File(app.filesDir.parentFile, "shared_prefs"),
                                    "${app.packageName}_preferences.xml"
                                ).outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            InternalDatabase.DB_NAME -> {
                                database.checkpoint()
                                database.delegate.close()
                                FileOutputStream(database.delegate.openHelper.writableDatabase.path).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                        entry = inputStream.nextEntry
                    }
                }
            }
            app.stopService(Intent(app, MusicService::class.java))
            app.filesDir.resolve(SongPlayer.PERSISTENT_QUEUE_FILE).delete()
            exitProcess(0)
        }.onFailure {
            Toast.makeText(app, R.string.message_restore_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
