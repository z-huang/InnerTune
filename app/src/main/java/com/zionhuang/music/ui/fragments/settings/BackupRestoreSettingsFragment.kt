package com.zionhuang.music.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zionhuang.music.R
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.MusicDatabase.Companion.DB_NAME
import com.zionhuang.music.db.checkpoint
import com.zionhuang.music.extensions.zipInputStream
import com.zionhuang.music.extensions.zipOutputStream
import com.zionhuang.music.playback.SongPlayer.Companion.PERSISTENT_QUEUE_FILE
import com.zionhuang.music.ui.activities.MainActivity
import com.zionhuang.music.ui.fragments.base.BaseSettingsFragment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import kotlin.system.exitProcess


class BackupRestoreSettingsFragment : BaseSettingsFragment() {
    private val wantToBackup = mutableListOf(/* Preferences */ true, /* Database */ true)
    private val wantToRestore = mutableListOf(/* Preferences */ true, /* Database */ true)
    private val backupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            requireContext().applicationContext.contentResolver.openOutputStream(uri)?.buffered()?.zipOutputStream()?.use { outputStream ->
                if (wantToBackup[0]) { // backup preferences
                    File(File(requireContext().filesDir.parentFile, "shared_prefs"), "${requireContext().packageName}_preferences.xml").inputStream().buffered().use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(PREF_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }
                if (wantToBackup[1]) { // backup database
                    val database = MusicDatabase.getInstance(requireContext())
                    database.checkpoint()
                    FileInputStream(database.openHelper.writableDatabase.path).use { inputStream ->
                        outputStream.putNextEntry(ZipEntry(DB_NAME))
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }.onSuccess {
            Toast.makeText(requireContext(), R.string.message_backup_create_success, LENGTH_SHORT).show()
        }.onFailure {
            Toast.makeText(requireContext(), R.string.message_backup_create_failed, LENGTH_SHORT).show()
        }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        runCatching {
            requireContext().applicationContext.contentResolver.openInputStream(uri)?.zipInputStream()?.use { inputStream ->
                var entry = inputStream.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        PREF_NAME -> if (wantToRestore[0]) {
                            File(File(requireContext().filesDir.parentFile, "shared_prefs"), "${requireContext().packageName}_preferences.xml").outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        DB_NAME -> if (wantToRestore[1]) {
                            val database = MusicDatabase.getInstance(requireContext())
                            database.checkpoint()
                            database.close()
                            FileOutputStream(database.openHelper.writableDatabase.path).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                    entry = inputStream.nextEntry
                }
            }
            requireContext().filesDir.resolve(PERSISTENT_QUEUE_FILE).delete()
            requireContext().startActivity(Intent(requireContext(), MainActivity::class.java))
            exitProcess(0)
        }.onFailure {
            Toast.makeText(requireContext(), R.string.message_restore_failed, LENGTH_SHORT).show()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_backup_restore)

        findPreference<Preference>(getString(R.string.pref_backup))?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_title_choose_backup_content)
                .setMultiChoiceItems(arrayOf(
                    getString(R.string.choice_preferences),
                    getString(R.string.choice_database)
                ), wantToBackup.toBooleanArray()) { dialog, which, isChecked ->
                    wantToBackup[which] = isChecked
                    if (dialog is AlertDialog) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !wantToBackup.all { !it }
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    backupLauncher.launch("${getString(R.string.app_name)}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}.backup")
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
        findPreference<Preference>(getString(R.string.pref_restore))?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_title_choose_restore_content)
                .setMultiChoiceItems(arrayOf(
                    getString(R.string.choice_preferences),
                    getString(R.string.choice_database)
                ), wantToRestore.toBooleanArray()) { dialog, which, isChecked ->
                    wantToRestore[which] = isChecked
                    if (dialog is AlertDialog) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !wantToBackup.all { !it }
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    restoreLauncher.launch(arrayOf("application/octet-stream"))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            true
        }
    }

    companion object {
        const val PREF_NAME = "preferences.xml"
    }
}