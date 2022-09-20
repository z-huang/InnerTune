package com.zionhuang.music.models.sortInfo

import android.content.Context
import com.zionhuang.music.R
import com.zionhuang.music.extensions.booleanFlow
import com.zionhuang.music.extensions.enumFlow
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.enumPreference

object AlbumSortInfoPreference : SortInfoPreference<AlbumSortType>() {
    val context: Context get() = getApplication()
    override var type by enumPreference(context, R.string.pref_album_sort_type, AlbumSortType.CREATE_DATE)
    override var isDescending by Preference(context, R.string.pref_album_sort_descending, true)
    override val typeFlow = context.sharedPreferences.enumFlow(context.getString(R.string.pref_album_sort_type), AlbumSortType.CREATE_DATE)
    override val isDescendingFlow = context.sharedPreferences.booleanFlow(context.getString(R.string.pref_album_sort_descending), true)
}