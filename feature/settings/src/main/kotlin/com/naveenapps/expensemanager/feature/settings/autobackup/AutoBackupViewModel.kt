package com.naveenapps.expensemanager.feature.settings.autobackup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // Set when the switch was turned on before a folder was chosen: picking one finishes enabling.
    private var enableAfterFolderPicked = false

    init {
        autoBackupRepository.getSettings()
            .onEach { settings -> _state.update { it.copy(settings = settings) } }
            .launchIn(viewModelScope)
    }

    fun processAction(action: AutoBackupAction) {
        when (action) {
            AutoBackupAction.ClosePage -> appComposeNavigator.popBackStack()
            AutoBackupAction.ToggleEnabled -> toggleEnabled()
            AutoBackupAction.ChooseFolder -> sendEvent(AutoBackupEvent.PickFolder)
            is AutoBackupAction.FolderSelected -> folderSelected(action.treeUri)
            is AutoBackupAction.SelectFrequency -> selectFrequency(action.frequency)
            AutoBackupAction.BackupNow -> backupNow()
        }
    }

    private fun toggleEnabled() {
        val settings = _state.value.settings ?: return
        when {
            settings.isEnabled -> viewModelScope.launch {
                autoBackupRepository.setEnabled(false)
                autoBackupScheduler.cancel()
                analyticsRepository.logEvent(AnalyticsEvents.AUTO_BACKUP_DISABLED, emptyMap())
            }

            settings.folderUri == null -> {
                enableAfterFolderPicked = true
                sendEvent(AutoBackupEvent.PickFolder)
            }

            else -> viewModelScope.launch { enable(settings.frequency) }
        }
    }

    private fun folderSelected(treeUri: String?) {
        val shouldEnable = enableAfterFolderPicked
        enableAfterFolderPicked = false
        if (treeUri == null) return
        viewModelScope.launch {
            if (!autoBackupRepository.setFolder(treeUri)) {
                _event.send(AutoBackupEvent.ShowMessage(R.string.auto_backup_folder_error))
                return@launch
            }
            val settings = _state.value.settings
            if (shouldEnable && settings?.isEnabled != true) {
                enable(settings?.frequency ?: AutoBackupFrequency.DAILY)
            }
        }
    }

    private suspend fun enable(frequency: AutoBackupFrequency) {
        autoBackupRepository.setEnabled(true)
        // The first periodic run starts right away (when the battery and storage allow), so
        // turning this on gives a fresh backup without waiting a whole period.
        autoBackupScheduler.schedule(frequency, replaceExisting = true)
        analyticsRepository.logEvent(
            AnalyticsEvents.AUTO_BACKUP_ENABLED,
            mapOf(AnalyticsParams.FREQUENCY to frequency.name.lowercase()),
        )
    }

    private fun selectFrequency(frequency: AutoBackupFrequency) {
        val settings = _state.value.settings ?: return
        if (settings.frequency == frequency) return
        viewModelScope.launch {
            autoBackupRepository.setFrequency(frequency)
            if (settings.isEnabled) autoBackupScheduler.schedule(frequency, replaceExisting = true)
        }
    }

    private fun backupNow() {
        if (_state.value.isBackingUp || _state.value.settings?.folderUri == null) return
        _state.update { it.copy(isBackingUp = true) }
        viewModelScope.launch {
            val result = autoBackupRepository.backupNow()
            _state.update { it.copy(isBackingUp = false) }
            val message = when (result) {
                AutoBackupResult.SUCCESS -> R.string.backup_success
                AutoBackupResult.FOLDER_UNAVAILABLE -> R.string.auto_backup_folder_unavailable
                AutoBackupResult.FAILED, AutoBackupResult.SKIPPED -> R.string.backup_failed
            }
            analyticsRepository.logEvent(
                if (result == AutoBackupResult.SUCCESS) AnalyticsEvents.AUTO_BACKUP_COMPLETED else AnalyticsEvents.AUTO_BACKUP_FAILED,
                mapOf(AnalyticsParams.SOURCE to "manual"),
            )
            _event.send(AutoBackupEvent.ShowMessage(message))
        }
    }

    private fun sendEvent(event: AutoBackupEvent) {
        viewModelScope.launch { _event.send(event) }
    }
}
