package com.zionhuang.music.models.sortInfo

data class SortInfo<T : SortType>(
    override val type: T,
    override val isDescending: Boolean,
) : ISortInfo<T>

interface SortType

enum class SongSortType : SortType {
    CREATE_DATE, NAME, ARTIST, PLAY_TIME
}

enum class ArtistSortType : SortType {
    CREATE_DATE, NAME, SONG_COUNT
}

enum class AlbumSortType : SortType {
    CREATE_DATE, NAME, ARTIST, YEAR, SONG_COUNT, LENGTH
}

enum class PlaylistSortType : SortType {
    CREATE_DATE, NAME, SONG_COUNT
}