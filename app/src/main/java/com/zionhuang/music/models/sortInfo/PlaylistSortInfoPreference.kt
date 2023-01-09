package com.zionhuang.music.models.sortInfo

import android.content.Context
import com.zionhuang.music.constants.PLAYLIST_SORT_DESCENDING
import com.zionhuang.music.constants.PLAYLIST_SORT_TYPE
import com.zionhuang.music.extensions.booleanFlow
import com.zionhuang.music.extensions.enumFlow
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.Preference
import com.zionhuang.music.utils.enumPreference

object PlaylistSortInfoPreference : SortInfoPreference<PlaylistSortType>() {
    val context: Context get() = getApplication()
    override var type by enumPreference(context, PLAYLIST_SORT_TYPE, PlaylistSortType.CREATE_DATE)
    override var isDescending by Preference(context, PLAYLIST_SORT_DESCENDING, true)
    override val typeFlow = context.sharedPreferences.enumFlow(PLAYLIST_SORT_TYPE, PlaylistSortType.CREATE_DATE)
    override val isDescendingFlow = context.sharedPreferences.booleanFlow(PLAYLIST_SORT_DESCENDING, true)
}