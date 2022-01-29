package com.zionhuang.music.models

import android.os.Parcelable
import com.zionhuang.music.models.base.ISortInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortInfo(
    override val type: Int,
    override val isDescending: Boolean,
) : ISortInfo, Parcelable