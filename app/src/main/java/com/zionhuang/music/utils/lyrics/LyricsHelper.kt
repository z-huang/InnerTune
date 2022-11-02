package com.zionhuang.music.utils.lyrics

import android.content.Context
import android.util.LruCache
import com.zionhuang.music.db.entities.LyricsEntity
import com.zionhuang.music.lyrics.KuGouLyricsProvider
import com.zionhuang.music.lyrics.YouTubeLyricsProvider
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.repos.SongRepository

object LyricsHelper {
    private val lyricsProviders = listOf(KuGouLyricsProvider, YouTubeLyricsProvider)

    private const val MAX_CACHE_SIZE = 10
    private val cache = LruCache<String, List<LyricsResult>>(MAX_CACHE_SIZE)

    suspend fun loadLyrics(context: Context, mediaMetadata: MediaMetadata) {
        val songRepository = SongRepository(context)
        val cached = cache.get(mediaMetadata.id)?.firstOrNull()
        if (cached != null) {
            songRepository.upsert(LyricsEntity(mediaMetadata.id, cached.lyrics))
            return
        }
        lyricsProviders.forEach { provider ->
            if (provider.isEnabled(context)) {
                provider.getLyrics(
                    mediaMetadata.id,
                    mediaMetadata.title,
                    mediaMetadata.artists.joinToString { it.name },
                    mediaMetadata.duration
                ).onSuccess { lyrics ->
                    songRepository.upsert(LyricsEntity(mediaMetadata.id, lyrics))
                    return
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
        songRepository.upsert(LyricsEntity(mediaMetadata.id, LyricsEntity.LYRICS_NOT_FOUND))
    }

    suspend fun getAllLyrics(context: Context, mediaId: String?, songTitle: String, songArtists: String, duration: Int): List<LyricsResult> {
        val cacheKey = "$songArtists-$songTitle".replace(" ", "")
        return cache.get(cacheKey) ?: lyricsProviders.flatMap { provider ->
            if (provider.isEnabled(context)) {
                provider.getAllLyrics(mediaId, songTitle, songArtists, duration).getOrNull().orEmpty().map {
                    LyricsResult(provider.name, it)
                }
            } else {
                emptyList()
            }
        }.also {
            cache.put(cacheKey, it)
        }
    }

    data class LyricsResult(
        val providerName: String,
        val lyrics: String,
    )
}