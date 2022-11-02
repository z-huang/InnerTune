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
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.startForegroundService
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media.session.MediaButtonReceiver
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zionhuang.music.R
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MusicService : LifecycleMediaBrowserService() {
    private val binder = MusicBinder()
    private val songRepository by lazy { SongRepository(this) }
    private lateinit var songPlayer: SongPlayer

    override fun onCreate() {
        super.onCreate()
        songPlayer = SongPlayer(this, lifecycleScope, object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }

            override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
                if (ongoing) {
                    startForegroundService(this@MusicService, Intent(this@MusicService, MusicService::class.java))
                    startForeground(notificationId, notification)
                } else {
                    stopForeground(0)
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
        val sessionToken: MediaSessionCompat.Token
            get() = songPlayer.mediaSession.sessionToken

        val songPlayer: SongPlayer
            get() = this@MusicService.songPlayer

        val cache: SimpleCache
            get() = this@MusicService.songPlayer.cache
    }


    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot = BrowserRoot(ROOT, null)

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) = runBlocking {
        when (parentId) {
            ROOT -> result.sendResult(mutableListOf(
                mediaBrowserItem(SONG, getString(R.string.title_songs), null, drawableUri(R.drawable.ic_music_note)),
                mediaBrowserItem(ARTIST, getString(R.string.title_artists), null, drawableUri(R.drawable.ic_artist)),
                mediaBrowserItem(ALBUM, getString(R.string.title_albums), null, drawableUri(R.drawable.ic_album)),
                mediaBrowserItem(PLAYLIST, getString(R.string.title_playlists), null, drawableUri(R.drawable.ic_queue_music))
            ))
            SONG -> {
                result.detach()
                result.sendResult(songRepository.getAllSongs(SongSortInfoPreference).flow.first().map {
                    MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(this@MusicService), FLAG_PLAYABLE)
                }.toMutableList())
            }
            ARTIST -> {
                result.detach()
                result.sendResult(songRepository.getAllArtists(ArtistSortInfoPreference).flow.first().map { artist ->
                    mediaBrowserItem("$ARTIST/${artist.id}", artist.artist.name, resources.getQuantityString(R.plurals.song_count, artist.songCount, artist.songCount), artist.artist.thumbnailUrl?.toUri())
                }.toMutableList())
            }
            ALBUM -> {
                result.detach()
                result.sendResult(songRepository.getAllAlbums(AlbumSortInfoPreference).flow.first().map { album ->
                    mediaBrowserItem("$ALBUM/${album.id}", album.album.title, album.artists.joinToString(), album.album.thumbnailUrl?.toUri())
                }.toMutableList())
            }
            PLAYLIST -> {
                result.detach()
                val likedSongCount = songRepository.getLikedSongCount().first()
                val downloadedSongCount = songRepository.getDownloadedSongCount().first()
                result.sendResult((listOf(
                    mediaBrowserItem("$PLAYLIST/$LIKED_PLAYLIST_ID", getString(R.string.liked_songs), resources.getQuantityString(R.plurals.song_count, likedSongCount, likedSongCount), drawableUri(R.drawable.ic_favorite)),
                    mediaBrowserItem("$PLAYLIST/$DOWNLOADED_PLAYLIST_ID", getString(R.string.downloaded_songs), resources.getQuantityString(R.plurals.song_count, downloadedSongCount, downloadedSongCount), drawableUri(R.drawable.ic_save_alt))
                ) + songRepository.getAllPlaylists(PlaylistSortInfoPreference).flow.first().filter { it.playlist.isLocalPlaylist }.map { playlist ->
                    mediaBrowserItem("$PLAYLIST/${playlist.id}", playlist.playlist.name, resources.getQuantityString(R.plurals.song_count, playlist.songCount, playlist.songCount), playlist.playlist.thumbnailUrl?.toUri() ?: playlist.thumbnails.firstOrNull()?.toUri())
                }).toMutableList())
            }
            else -> when {
                parentId.startsWith("$ARTIST/") -> {
                    result.detach()
                    result.sendResult(songRepository.getArtistSongs(parentId.removePrefix("$ARTIST/"), SongSortInfoPreference).flow.first().map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(this@MusicService), FLAG_PLAYABLE)
                    }.toMutableList())
                }
                parentId.startsWith("$ALBUM/") -> {
                    result.detach()
                    result.sendResult(songRepository.getAlbumSongs(parentId.removePrefix("$ALBUM/")).map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(this@MusicService), FLAG_PLAYABLE)
                    }.toMutableList())
                }
                parentId.startsWith("$PLAYLIST/") -> {
                    result.detach()
                    result.sendResult(when (val playlistId = parentId.removePrefix("$PLAYLIST/")) {
                        LIKED_PLAYLIST_ID -> songRepository.getLikedSongs(SongSortInfoPreference)
                        DOWNLOADED_PLAYLIST_ID -> songRepository.getDownloadedSongs(SongSortInfoPreference)
                        else -> songRepository.getPlaylistSongs(playlistId)
                    }.flow.first().map {
                        MediaBrowserCompat.MediaItem(it.toMediaMetadata().copy(id = "$parentId/${it.id}").toMediaDescription(this@MusicService), FLAG_PLAYABLE)
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