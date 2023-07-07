package com.zionhuang.music.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.MAX_GET_QUEUE_SIZE
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.MainActivity
import com.zionhuang.music.R
import com.zionhuang.music.db.InternalDatabase
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.generatePlaylistId
import com.zionhuang.music.db.entities.PlaylistSongMap
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.zipInputStream
import com.zionhuang.music.extensions.zipOutputStream
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.playback.MusicService
import com.zionhuang.music.playback.MusicService.Companion.PERSISTENT_QUEUE_FILE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    fun backup(context: Context, uri: Uri) {
        runCatching {
            context.applicationContext.contentResolver.openOutputStream(uri)?.use {
                it.buffered().zipOutputStream().use { outputStream ->
                    (context.filesDir / "datastore" / SETTINGS_FILENAME).inputStream().buffered().use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(SETTINGS_FILENAME))
                        inputStream.copyTo(outputStream)
                    }
                    runBlocking(Dispatchers.IO) {
                        database.checkpoint()
                    }
                    FileInputStream(database.openHelper.writableDatabase.path).use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(InternalDatabase.DB_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }.onSuccess {
            Toast.makeText(context, R.string.backup_create_success, Toast.LENGTH_SHORT).show()
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(context, R.string.backup_create_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun restore(context: Context, uri: Uri) {
        runCatching {
            context.applicationContext.contentResolver.openInputStream(uri)?.use {
                it.zipInputStream().use { inputStream ->
                    var entry = inputStream.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            SETTINGS_FILENAME -> {
                                (context.filesDir / "datastore" / SETTINGS_FILENAME).outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                            }

                            InternalDatabase.DB_NAME -> {
                                runBlocking(Dispatchers.IO) {
                                    database.checkpoint()
                                }
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
            context.filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
            context.startActivity(Intent(context, MainActivity::class.java))
            exitProcess(0)
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_SHORT).show()
        }
    }

    fun import(context: Context, uri: Uri) {
        runCatching {
            val videoIds = mutableListOf<String>()
            context.applicationContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                val br = inputStream.bufferedReader()
                repeat(8) {
                    br.readLine()
                }
                var line = br.readLine()
                while (line != null) {
                    line.split(",").firstOrNull()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            videoIds.add(it.trim())
                        }
                    line = br.readLine()
                }
            }
            val playlistName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }?.removeSuffix(".csv") ?: context.getString(R.string.imported_playlist)
            viewModelScope.launch {
                val songs = videoIds.chunked(MAX_GET_QUEUE_SIZE).flatMap {
                    withContext(Dispatchers.IO) {
                        YouTube.queue(videoIds = it)
                    }.getOrNull().orEmpty()
                }
                database.transaction {
                    val playlistId = generatePlaylistId()
                    var position = 0
                    insert(
                        PlaylistEntity(
                            id = playlistId,
                            name = playlistName
                        )
                    )
                    songs.map(SongItem::toMediaMetadata)
                        .onEach(::insert)
                        .forEach {
                            insert(
                                PlaylistSongMap(
                                    playlistId = playlistId,
                                    songId = it.id,
                                    position = position++
                                )
                            )
                        }
                }
                Toast.makeText(context, context.resources.getQuantityString(R.plurals.import_success, songs.size, playlistName, songs.size), Toast.LENGTH_SHORT).show()
            }
        }.onFailure {
            it.printStackTrace()
            Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val SETTINGS_FILENAME = "settings.preferences_pb"
    }
}
