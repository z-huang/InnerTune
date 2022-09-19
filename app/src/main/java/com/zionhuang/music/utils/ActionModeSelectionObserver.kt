package com.zionhuang.music.utils

import android.app.Activity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.recyclerview.selection.SelectionTracker
import com.zionhuang.music.R

class ActionModeSelectionObserver<T : Any>(
    private val activity: Activity,
    private val tracker: SelectionTracker<T>,
    @MenuRes private val menuRes: Int,
    val onActionItemClicked: (MenuItem) -> Boolean,
) : SelectionTracker.SelectionObserver<T>() {
    private var actionMode: ActionMode? = null

    override fun onItemStateChanged(key: T, selected: Boolean) {
        if (!tracker.hasSelection()) {
            actionMode?.finish()
            return
        }
        if (actionMode == null) {
            actionMode = activity.startActionMode(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    MenuInflater(activity).inflate(menuRes, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

                override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
                    val res = onActionItemClicked(item)
                    mode?.finish()
                    return res
                }

                override fun onDestroyActionMode(mode: ActionMode?) {
                    tracker.clearSelection()
                    actionMode = null
                }
            })
        }
        actionMode?.title = activity.resources.getQuantityString(
            R.plurals.n_selected,
            tracker.selection.size(),
            tracker.selection.size()
        )
    }
}

fun <T : Any> SelectionTracker<T>.addActionModeObserver(
    activity: Activity,
    @MenuRes menuRes: Int,
    onActionItemClicked: (MenuItem) -> Boolean,
) = addObserver(
    ActionModeSelectionObserver(
        activity,
        this,
        menuRes,
        onActionItemClicked
    )
)
