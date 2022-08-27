package com.zionhuang.music.models

import android.content.Context
import com.zionhuang.music.R
import com.zionhuang.music.extensions.booleanFlow
import com.zionhuang.music.extensions.enumFlow
import com.zionhuang.music.extensions.getApplication
import com.zionhuang.music.extensions.sharedPreferences
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.enumPreference
import kotlinx.coroutines.flow.combine

object ArtistSortInfoPreference : IMutableSortInfo<ArtistSortType> {
    val context: Context get() = getApplication()
    override var type: ArtistSortType by enumPreference(context, R.string.pref_artist_sort_type, ArtistSortType.CREATE_DATE)
    override var isDescending by Preference(context, R.string.pref_artist_sort_descending, true)
    private val typeFlow = context.sharedPreferences.enumFlow(context.getString(R.string.pref_artist_sort_type), ArtistSortType.CREATE_DATE)
    private val isDescendingFlow = context.sharedPreferences.booleanFlow(context.getString(R.string.pref_artist_sort_descending), true)

    val flow = typeFlow.combine(isDescendingFlow) { type, isDescending ->
        SortInfo(type, isDescending)
    }
}