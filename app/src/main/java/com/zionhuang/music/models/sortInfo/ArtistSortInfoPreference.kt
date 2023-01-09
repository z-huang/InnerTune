package com.zionhuang.music.models.sortInfo

import android.content.Context
import com.zionhuang.music.constants.ARTIST_SORT_DESCENDING
import com.zionhuang.music.constants.ARTIST_SORT_TYPE
import com.zionhuang.music.extensions.booleanFlow
import com.zionhuang.music.extensions.enumFlow
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.utils.Preference
import com.zionhuang.music.utils.enumPreference

object ArtistSortInfoPreference : SortInfoPreference<ArtistSortType>() {
    val context: Context get() = getApplication()
    override var type: ArtistSortType by enumPreference(context, ARTIST_SORT_TYPE, ArtistSortType.CREATE_DATE)
    override var isDescending by Preference(context, ARTIST_SORT_DESCENDING, true)
    override val typeFlow = context.sharedPreferences.enumFlow(ARTIST_SORT_TYPE, ArtistSortType.CREATE_DATE)
    override val isDescendingFlow = context.sharedPreferences.booleanFlow(ARTIST_SORT_DESCENDING, true)
}