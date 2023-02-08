package com.zionhuang.music.playback

import android.app.Notification
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zionhuang.music.R
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.lyrics.LyricsHelper
import com.zionhuang.music.models.toMediaMetadata
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {
    @Inject
    lateinit var database: MusicDatabase

    @Inject
    lateinit var lyricsHelper: LyricsHelper
    private val coroutineScope = CoroutineScope(Dispatchers.Main) + Job()
    private val binder = MusicBinder()
    private lateinit var songPlayer: SongPlayer

    override fun onCreate() {
        super.onCreate()
        songPlayer = SongPlayer(this, database, lyricsHelper, coroutineScope, object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }

            override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                if (ongoing) {
                    startForegroundService(this@MusicService, Intent(this@MusicService, MusicService::class.java))
                    startForeground(notificationId, notification)
                } else {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
            }
        })
        sessionToken = songPlayer.mediaSession.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        MediaButtonReceiver.handleIntent(songPlayer.mediaSession, intent)
        return START_STICKY
    }

    override fun onDestroy() {
        songPlayer.onDestroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        val superBinder = super.onBind(intent)
        return when (intent.action) {
            SERVICE_INTERFACE -> superBinder
            else -> binder
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    inner class MusicBinder : Binder() {
        val player: ExoPlayer
            get() = this@MusicService.songPlayer.player

        val songPlayer: SongPlayer
            get() = this@MusicService.songPlayer

        val cache: SimpleCache
            get() = this@MusicService.songPlayer.cache
    }


    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot = BrowserRoot(ROOT, null)

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) = runBlocking {
        when (parentId) {
            ROOT -> result.sendResult(mutableListOf(
                mediaBrowserItem(SONG, getString(R.string.songs), null, drawableUri(R.drawable.ic_music_note)),
                mediaBrowserItem(ARTIST, getString(R.string.artists), null, drawableUri(R.drawable.ic_artist)),
                mediaBrowserItem(ALBUM, getString(R.string.albums), null, drawableUri(R.drawable.ic_album)),
                mediaBrowserItem(PLAYLIST, getString(R.string.playlists), null, drawableUri(R.drawable.ic_queue_music))
            ))
            SONG -> {
                result.detach()
                result.sendResult(database.songsByCreateDateDesc().first().map {
                    MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(), FLAG_PLAYABLE)
                }.toMutableList())
            }
            ARTIST -> {
                result.detach()
                result.sendResult(database.artistsByCreateDateDesc().first().map { artist ->
                    mediaBrowserItem("$ARTIST/${artist.id}", artist.artist.name, resources.getQuantityString(R.plurals.n_song, artist.songCount, artist.songCount), artist.artist.thumbnailUrl?.toUri())
                }.toMutableList())
            }
            ALBUM -> {
                result.detach()
                result.sendResult(database.albumsByCreateDateDesc().first().map { album ->
                    mediaBrowserItem("$ALBUM/${album.id}", album.album.title, album.artists.joinToString(), album.album.thumbnailUrl?.toUri())
                }.toMutableList())
            }
            PLAYLIST -> {
                result.detach()
                val likedSongCount = database.likedSongsCount().first()
                val downloadedSongCount = database.downloadedSongsCount().first()
                result.sendResult((listOf(
                    mediaBrowserItem("$PLAYLIST/$LIKED_PLAYLIST_ID", getString(R.string.liked_songs), resources.getQuantityString(R.plurals.n_song, likedSongCount, likedSongCount), drawableUri(R.drawable.ic_favorite)),
                    mediaBrowserItem("$PLAYLIST/$DOWNLOADED_PLAYLIST_ID", getString(R.string.downloaded_songs), resources.getQuantityString(R.plurals.n_song, downloadedSongCount, downloadedSongCount), drawableUri(R.drawable.ic_save_alt))
                ) + database.playlistsByCreateDateDesc().first().map { playlist ->
                    mediaBrowserItem("$PLAYLIST/${playlist.id}", playlist.playlist.name, resources.getQuantityString(R.plurals.n_song, playlist.songCount, playlist.songCount), playlist.thumbnails.firstOrNull()?.toUri())
                }).toMutableList())
            }
            else -> when {
                parentId.startsWith("$ARTIST/") -> {
                    result.detach()
                    result.sendResult(database.artistSongsByCreateDateDesc(parentId.removePrefix("$ARTIST/")).first().map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(), FLAG_PLAYABLE)
                    }.toMutableList())
                }
                parentId.startsWith("$ALBUM/") -> {
                    result.detach()
                    result.sendResult(database.albumSongs(parentId.removePrefix("$ALBUM/")).first().map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(), FLAG_PLAYABLE)
                    }.toMutableList())
                }
                parentId.startsWith("$PLAYLIST/") -> {
                    result.detach()
                    result.sendResult(when (val playlistId = parentId.removePrefix("$PLAYLIST/")) {
                        LIKED_PLAYLIST_ID -> database.likedSongs(SongSortType.CREATE_DATE, true)
                        DOWNLOADED_PLAYLIST_ID -> database.downloadedSongs(SongSortType.CREATE_DATE, true)
                        else -> database.playlistSongs(playlistId)
                    }.first().map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(), FLAG_PLAYABLE)
                    }.toMutableList())
                }
                else -> {
                    result.sendResult(mutableListOf())
                }
            }
        }
    }

    private fun drawableUri(@DrawableRes id: Int) = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(id))
        .appendPath(resources.getResourceTypeName(id))
        .appendPath(resources.getResourceEntryName(id))
        .build()

    private fun mediaBrowserItem(id: String, title: String, subtitle: String?, iconUri: Uri?, flags: Int = FLAG_BROWSABLE) =
        MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(id)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setIconUri(iconUri)
                .build(),
            flags
        )

    companion object {
        const val ROOT = "root"
        const val SONG = "song"
        const val ARTIST = "artist"
        const val ALBUM = "album"
        const val PLAYLIST = "playlist"
    }
}
