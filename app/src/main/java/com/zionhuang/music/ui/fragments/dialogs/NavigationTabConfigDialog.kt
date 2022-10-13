package com.zionhuang.music.ui.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.utils.NavigationTabHelper

class NavigationTabConfigDialog : AppCompatDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val enabledTabs = NavigationTabHelper.getConfig(requireContext())
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.pref_customize_navigation_tabs)
            .setMultiChoiceItems(R.array.bottom_nav_items, enabledTabs) { dialog, index, newState ->
                enabledTabs[index] = newState
                if (dialog is AlertDialog) {
                    dialog.getButton(BUTTON_POSITIVE).isEnabled = !enabledTabs.none { it }
                }
            }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                if (!NavigationTabHelper.getConfig(requireContext()).contentEquals(enabledTabs)) {
                    NavigationTabHelper.setConfig(requireContext(), enabledTabs)
                    Toast.makeText(requireContext(), R.string.pref_restart_title, LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}
