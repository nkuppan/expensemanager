package com.naveenapps.expensemanager.feature.settings.autobackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupScheduler
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

/**
 * The Backup page: "Back up now", where backups go, the automatic schedule and the recent
 * backups, each of which can be shared (optionally protected with a passphrase).
 */
class AutoBackupViewModel(
    private val autoBackupRepository: AutoBackupRepository,
    private val autoBackupScheduler: AutoBackupScheduler,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AutoBackupState())
    val state = _state.asStateFlow()

    private val _event = Channel<AutoBackupEvent>()
    val event = _event.receiveAsFlow()

    init {
        val settings = autoBackupRepository.getSettings()
        settings
            .onEach { value -> _state.update { it.copy(settings = value) } }
            .launchIn(viewModelScope)
        // A new backup (here or from the schedule) or another folder changes the list.
        settings
            .map { it.folderUri to it.lastBackupAt }
            .distinctUntilChanged()
            .onEach { refreshBackups() }
            .launchIn(viewModelScope)
    }

    fun processAction(action: AutoBackupAction) {
        when (action) {
            AutoBackupAction.ClosePage -> appComposeNavigator.popBackStack()
            AutoBackupAction.BackupNow -> backupNow()
            AutoBackupAction.ChooseFolder -> sendEvent(AutoBackupEvent.PickFolder)
            is AutoBackupAction.FolderSelected -> folderSelected(action.treeUri)
            AutoBackupAction.ToggleEnabled -> toggleEnabled()
            is AutoBackupAction.SelectFrequency -> selectFrequency(action.frequency)
            is AutoBackupAction.Share -> _state.update { it.copy(shareCandidate = action.backup) }
            is AutoBackupAction.SubmitSharePassphrase -> share(action.passphrase)
            AutoBackupAction.DismissShare -> _state.update { it.copy(shareCandidate = null) }
        }
    }

    private suspend fun refreshBackups() {
        val backups = autoBackupRepository.getBackups()
        _state.update { it.copy(backups = backups) }
    }

    private fun backupNow() {
        if (_state.value.isBackingUp) return
        _state.update { it.copy(isBackingUp = true) }
        viewModelScope.launch {
            val result = autoBackupRepository.backupNow(BackupKind.MANUAL)
            _state.update { it.copy(isBackingUp = false) }
            val message = when (result) {
                AutoBackupResult.SUCCESS -> R.string.backup_success
                AutoBackupResult.FOLDER_UNAVAILABLE -> R.string.auto_backup_folder_unavailable
                AutoBackupResult.FAILED, AutoBackupResult.SKIPPED -> R.string.backup_failed
            }
            analyticsRepository.logEvent(
                if (result == AutoBackupResult.SUCCESS) AnalyticsEvents.BACKUP_COMPLETED else AnalyticsEvents.AUTO_BACKUP_FAILED,
                mapOf(AnalyticsParams.SOURCE to "manual"),
            )
            _event.send(AutoBackupEvent.ShowMessage(message))
        }
    }

    private fun folderSelected(treeUri: String?) {
        if (treeUri == null) return
        viewModelScope.launch {
            if (!autoBackupRepository.setFolder(treeUri)) {
                _event.send(AutoBackupEvent.ShowMessage(R.string.auto_backup_folder_error))
            }
        }
    }

    private fun toggleEnabled() {
        val settings = _state.value.settings ?: return
        viewModelScope.launch {
            if (settings.isEnabled) {
                autoBackupRepository.setEnabled(false)
                autoBackupScheduler.cancel()
                analyticsRepository.logEvent(AnalyticsEvents.AUTO_BACKUP_DISABLED, emptyMap())
            } else {
                autoBackupRepository.setEnabled(true)
                // The first periodic run starts right away (when the battery and storage allow),
                // so turning this on gives a fresh backup without waiting a whole period.
                autoBackupScheduler.schedule(settings.frequency, replaceExisting = true)
                analyticsRepository.logEvent(
                    AnalyticsEvents.AUTO_BACKUP_ENABLED,
                    mapOf(AnalyticsParams.FREQUENCY to settings.frequency.name.lowercase()),
                )
            }
        }
    }

    private fun selectFrequency(frequency: AutoBackupFrequency) {
        val settings = _state.value.settings ?: return
        if (settings.frequency == frequency) return
        viewModelScope.launch {
            autoBackupRepository.setFrequency(frequency)
            if (settings.isEnabled) autoBackupScheduler.schedule(frequency, replaceExisting = true)
        }
    }

    private fun share(passphrase: String) {
        val backup: BackupFile = _state.value.shareCandidate ?: return
        if (_state.value.isPreparingShare) return
        _state.update { it.copy(shareCandidate = null, isPreparingShare = true) }
        viewModelScope.launch {
            val uri = autoBackupRepository.prepareShare(backup, passphrase.ifEmpty { null })
            _state.update { it.copy(isPreparingShare = false) }
            _event.send(
                if (uri != null) AutoBackupEvent.ShareFile(uri) else AutoBackupEvent.ShowMessage(R.string.share_backup_failed),
            )
        }
    }

    private fun sendEvent(event: AutoBackupEvent) {
        viewModelScope.launch { _event.send(event) }
    }
}
