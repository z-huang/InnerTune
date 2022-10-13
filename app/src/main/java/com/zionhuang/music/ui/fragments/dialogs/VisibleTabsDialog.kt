package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.utils.NavBarHelper

class VisibleTabsDialog(
    private val onChange: () -> Unit
) : AppCompatDialogFragment() {
    var enabledItems = BooleanArray(5) { true }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val navBarHelper = NavBarHelper(requireContext())
        enabledItems = navBarHelper.getEnabledItems()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.visible_tabs)
            .setMultiChoiceItems(R.array.bottom_nav_items, enabledItems) { _, index, newState ->
                enabledItems[index] = newState
            }
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(BUTTON_POSITIVE).setOnClickListener { onConfirm() }
                }
            }
    }

    fun onConfirm() {
        if (enabledItems.filter { it }.isEmpty()) {
            Toast.makeText(context, R.string.visible_tabs_required, Toast.LENGTH_SHORT).show()
            return
        }
        val navBarHelper = NavBarHelper(requireContext())
        if (navBarHelper.getEnabledItems().contentEquals(enabledItems)) {
            dialog?.dismiss()
            return
        }
        navBarHelper.setEnabledItems(enabledItems)
        onChange.invoke()
        dialog?.dismiss()
    }
}
