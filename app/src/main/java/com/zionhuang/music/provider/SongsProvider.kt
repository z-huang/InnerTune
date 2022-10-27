package com.zionhuang.music.provider

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Document.MIME_TYPE_DIR
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import com.google.android.exoplayer2.util.FileTypes
import com.zionhuang.music.R
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.FileNotFoundException
import java.time.ZoneOffset

class SongsProvider : DocumentsProvider() {
    private lateinit var songRepository: SongRepository

    override fun onCreate(): Boolean {
        songRepository = SongRepository(context!!)
        return true
    }

    override fun queryRoots(projection: Array<String>?): Cursor =
        MatrixCursor(projection ?: ROOT_PROJECTION).apply {
            newRow()
                .add(Root.COLUMN_ROOT_ID, ROOT)
                .add(Root.COLUMN_DOCUMENT_ID, ROOT_DOC)
                .add(Root.COLUMN_TITLE, context!!.getString(R.string.app_name))
                .add(Root.COLUMN_ICON, R.drawable.ic_launcher_foreground)
                .add(Root.COLUMN_MIME_TYPES, "*/*")
                .add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_SEARCH)
        }

    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor = runBlocking {
        val cursor = MatrixCursor(projection ?: DOCUMENT_PROJECTION)
        when (documentId) {
            ROOT_DOC -> cursor.newRow()
                .add(Document.COLUMN_DOCUMENT_ID, documentId)
                .add(Document.COLUMN_DISPLAY_NAME, "Root")
                .add(Document.COLUMN_MIME_TYPE, MIME_TYPE_DIR)
//                .add(Document.COLUMN_FLAGS, FLAG_SUPPORTS_THUMBNAIL)
            else -> {
                val song = songRepository.getSongById(documentId).getValueAsync() ?: throw FileNotFoundException()
                val format = songRepository.getSongFormat(documentId).getValueAsync() ?: throw FileNotFoundException()
                cursor.newRow()
                    .add(Document.COLUMN_DOCUMENT_ID, documentId)
                    .add(Document.COLUMN_DISPLAY_NAME, song.song.title)
                    .add(Document.COLUMN_MIME_TYPE, format.mimeType)
                    .add(Document.COLUMN_SIZE, format.contentLength)
                    .add(Document.COLUMN_LAST_MODIFIED, song.song.modifyDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
//                    .add(Document.COLUMN_FLAGS, FLAG_SUPPORTS_THUMBNAIL)
            }
        }
        cursor
    }

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<String>?, sortOrder: String?): Cursor = runBlocking {
        val result = MatrixCursor(DOCUMENT_PROJECTION)
        when (parentDocumentId) {
            ROOT_DOC -> result.apply {
                songRepository.getDownloadedSongs(SongSortInfoPreference).flow.first().forEach { song ->
                    val format = songRepository.getSongFormat(song.id).getValueAsync()
                    if (format != null) {
                        newRow()
                            .add(Document.COLUMN_DOCUMENT_ID, song.id)
                            .add(Document.COLUMN_DISPLAY_NAME, "${song.song.title}${mimeToExt(format.mimeType)}")
                            .add(Document.COLUMN_MIME_TYPE, format.mimeType)
                            .add(Document.COLUMN_SIZE, format.contentLength)
                            .add(Document.COLUMN_LAST_MODIFIED, song.song.modifyDate.atZone(ZoneOffset.UTC).toInstant().toEpochMilli())
//                        .add(Document.COLUMN_FLAGS, FLAG_SUPPORTS_THUMBNAIL)
                    }
                }
            }
            else -> {}
        }
        result
    }

    override fun openDocument(documentId: String, mode: String?, signal: CancellationSignal?): ParcelFileDescriptor = runBlocking {
        val file = songRepository.getSongFile(documentId)
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }

    private fun mimeToExt(mimeType: String) = when (FileTypes.inferFileTypeFromMimeType(mimeType)) {
        FileTypes.AC3 -> ".ac3"
        FileTypes.AC4 -> ".ac4"
        FileTypes.ADTS -> ".adts"
        FileTypes.AMR -> ".amr"
        FileTypes.AVI -> ".avi"
        FileTypes.FLAC -> ".flac"
        FileTypes.FLV -> ".flv"
        FileTypes.JPEG -> ".jpg"
        FileTypes.MATROSKA -> ".webm"
        FileTypes.MIDI -> ".midi"
        FileTypes.MP3 -> ".mp3"
        FileTypes.MP4 -> ".m4a"
        FileTypes.OGG -> ".ogg"
        FileTypes.PS -> ".ps"
        FileTypes.TS -> ".ts"
        FileTypes.WAV -> ".wav"
        FileTypes.WEBVTT -> ".webvtt"
        else -> ""
    }

    companion object {
        private const val ROOT = "root"
        private const val ROOT_DOC = "root_dir"

        private val ROOT_PROJECTION: Array<String> = arrayOf(
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_ICON,
            Root.COLUMN_MIME_TYPES,
            Root.COLUMN_FLAGS,
            Root.COLUMN_AVAILABLE_BYTES
        )
        private val DOCUMENT_PROJECTION: Array<String> = arrayOf(
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS
        )
    }
}