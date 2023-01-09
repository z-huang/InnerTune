package com.zionhuang.music.models.sortInfo

import android.content.Context
import com.zionhuang.music.constants.ALBUM_SORT_DESCENDING
import com.zionhuang.music.constants.ALBUM_SORT_TYPE
import com.zionhuang.music.extensions.booleanFlow
import com.zionhuang.music.extensions.enumFlow
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.Preference
import com.zionhuang.music.utils.enumPreference

object AlbumSortInfoPreference : SortInfoPreference<AlbumSortType>() {
    val context: Context get() = getApplication()
    override var type by enumPreference(context, ALBUM_SORT_TYPE, AlbumSortType.CREATE_DATE)
    override var isDescending by Preference(context, ALBUM_SORT_DESCENDING, true)
    override val typeFlow = context.sharedPreferences.enumFlow(ALBUM_SORT_TYPE, AlbumSortType.CREATE_DATE)
    override val isDescendingFlow = context.sharedPreferences.booleanFlow(ALBUM_SORT_DESCENDING, true)
}