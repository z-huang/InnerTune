package com.zionhuang.music.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.checkpoint
import com.zionhuang.music.extensions.zipInputStream
import com.zionhuang.music.extensions.zipOutputStream
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.playback.SongPlayer
import com.zionhuang.music.ui.component.PreferenceEntry
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import kotlin.system.exitProcess

@Composable
fun BackupAndRestore() {
    val context = LocalContext.current
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.applicationContext.contentResolver.openOutputStream(uri)?.use {
                it.buffered().zipOutputStream().use { outputStream ->
                    File(
                        File(context.filesDir.parentFile, "shared_prefs"),
                        "${context.packageName}_preferences.xml"
                    ).inputStream().buffered().use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(PREF_NAME))
                        inputStream.copyTo(outputStream)
                    }
                    val database = MusicDatabase.getInstance(context)
                    database.checkpoint()
                    FileInputStream(database.openHelper.writableDatabase.path).use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(MusicDatabase.DB_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }.onSuccess {
            Toast.makeText(context, R.string.message_backup_create_success, Toast.LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(context, R.string.message_backup_create_failed, Toast.LENGTH_SHORT).show()
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.applicationContext.contentResolver.openInputStream(uri)?.use {
                it.zipInputStream().use { inputStream ->
                    var entry = inputStream.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            PREF_NAME -> {
                                File(
                                    File(context.filesDir.parentFile, "shared_prefs"),
                                    "${context.packageName}_preferences.xml"
                                ).outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                            MusicDatabase.DB_NAME -> {
                                val database = MusicDatabase.getInstance(context)
                                database.checkpoint()
                                database.close()
                                FileOutputStream(database.openHelper.writableDatabase.path).use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }
                        }
                        entry = inputStream.nextEntry
                    }
                }
            }
            context.stopService(Intent(context, MusicService::class.java))
            context.filesDir.resolve(SongPlayer.PERSISTENT_QUEUE_FILE).delete()
            exitProcess(0)
        }.onFailure {
            Toast.makeText(context, R.string.message_restore_failed, Toast.LENGTH_SHORT).show()
        }
    }
    Column(
        Modifier
            .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues())
            .verticalScroll(rememberScrollState())
    ) {
        PreferenceEntry(
            title = stringResource(R.string.pref_backup_title),
            icon = R.drawable.ic_backup,
            onClick = {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                backupLauncher.launch("${context.getString(R.string.app_name)}_${LocalDateTime.now().format(formatter)}.backup")
            }
        )
        PreferenceEntry(
            title = stringResource(R.string.pref_restore_title),
            icon = R.drawable.ic_restore,
            onClick = {
                restoreLauncher.launch(arrayOf("application/octet-stream"))
            }
        )
    }
}

const val PREF_NAME = "preferences.xml"