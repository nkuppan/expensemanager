package com.naveenapps.expensemanager.feature.settings.restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.BackupException
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.feature.settings.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The Restore page: pick any backup file, or one of the backups on this device. */
class RestoreViewModel(
    private val autoBackupRepository: AutoBackupRepository,
    private val backupRepository: BackupRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RestoreState())
    val state = _state.asStateFlow()

    private val _event = Channel<RestoreEvent>()
    val event = _event.receiveAsFlow()

    // The protected backup waiting for its passphrase; never persisted.
    private var pendingUri: String? = null

    init {
        autoBackupRepository.getSettings()
            .map { it.folderUri to it.lastBackupAt }
            .distinctUntilChanged()
            .onEach {
                val backups = autoBackupRepository.getBackups()
                _state.update { it.copy(backups = backups) }
            }
            .launchIn(viewModelScope)
    }

    fun processAction(action: RestoreAction) {
        when (action) {
            RestoreAction.ClosePage -> appComposeNavigator.popBackStack()

            RestoreAction.ChooseFile -> {
                if (_state.value.isRestoring) return
                viewModelScope.launch {
                    analyticsRepository.logEvent(AnalyticsEvents.RESTORE_STARTED, emptyMap())
                    _event.send(RestoreEvent.PickFile)
                }
            }

            // The warning on the page is the confirmation for a file the user went and picked.
            is RestoreAction.FileSelected -> action.uri?.let { restore(it, passphrase = null) }

            is RestoreAction.SelectBackup -> {
                if (_state.value.isRestoring) return
                _state.update { it.copy(confirmBackup = action.backup) }
            }

            RestoreAction.ConfirmRestore -> {
                val backup = _state.value.confirmBackup ?: return
                _state.update { it.copy(confirmBackup = null) }
                analyticsRepository.logEvent(AnalyticsEvents.RESTORE_STARTED, emptyMap())
                restore(backup.uri, passphrase = null)
            }

            RestoreAction.DismissConfirmation -> _state.update { it.copy(confirmBackup = null) }

            is RestoreAction.SubmitPassphrase -> {
                val uri = pendingUri ?: return
                if (action.passphrase.isEmpty()) return
                _state.update { it.copy(showPassphrasePrompt = false) }
                restore(uri, action.passphrase)
            }

            RestoreAction.DismissPassphrase -> {
                pendingUri = null
                _state.update { it.copy(showPassphrasePrompt = false) }
            }
        }
    }

    private fun restore(uri: String, passphrase: String?) {
        if (_state.value.isRestoring) return
        _state.update { it.copy(isRestoring = true) }
        viewModelScope.launch {
            try {
                when (val result = backupRepository.restoreData(uri, passphrase)) {
                    is Resource.Success -> {
                        pendingUri = null
                        analyticsRepository.logEvent(AnalyticsEvents.RESTORE_COMPLETED, emptyMap())
                        _event.send(RestoreEvent.RestartApp)
                    }

                    is Resource.Error -> {
                        val reason = (result.exception as? BackupException)?.reason
                        failureMessage(uri, reason)?.let { _event.send(RestoreEvent.ShowMessage(it)) }
                    }
                }
            } finally {
                _state.update { it.copy(isRestoring = false) }
            }
        }
    }

    /** Handles a failed restore; returns the message to show, or null for none. */
    private fun failureMessage(uri: String, reason: BackupException.Reason?): Int? = when (reason) {
        // Protected backup: ask for the passphrase and retry with the same file.
        BackupException.Reason.PASSPHRASE_REQUIRED -> {
            askForPassphrase(uri)
            null
        }

        BackupException.Reason.WRONG_PASSPHRASE -> {
            askForPassphrase(uri)
            R.string.restore_wrong_passphrase
        }

        BackupException.Reason.INVALID_FILE -> R.string.restore_invalid_file

        BackupException.Reason.NEWER_VERSION -> R.string.restore_newer_version

        else -> R.string.restore_failed
    }

    private fun askForPassphrase(uri: String) {
        pendingUri = uri
        _state.update { it.copy(showPassphrasePrompt = true) }
    }
}
