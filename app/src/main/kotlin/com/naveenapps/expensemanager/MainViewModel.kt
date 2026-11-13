package com.naveenapps.expensemanager

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.feature.theme.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    settingsRepository: SettingsRepository,
    analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            R.string.choose_theme,
        ),
    )
    val currentTheme = _currentTheme.asStateFlow()

    private val _onboardingStatus = MutableStateFlow<Boolean?>(null)
    val onboardingStatus = _onboardingStatus.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled = _isAppLockEnabled.asStateFlow()

    private val _isScreenshotBlocked = MutableStateFlow(false)
    val isScreenshotBlocked = _isScreenshotBlocked.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        // Once per Activity launch (the ViewModel survives rotation, so no double count).
        analyticsRepository.trackAppOpenEvent()

        getCurrentThemeUseCase.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            _onboardingStatus.value = getOnboardingStatusUseCase.invoke()
        }

        settingsRepository.isAppLockEnabled().onEach {
            _isAppLockEnabled.value = it
        }.launchIn(viewModelScope)

        settingsRepository.isScreenshotBlocked().onEach {
            _isScreenshotBlocked.value = it
        }.launchIn(viewModelScope)
    }

    // Set when the app was opened from a quick-add entry point; MainScreen opens the keypad
    // once navigation is ready and then clears it. Survives rotation (it's in the ViewModel).
    private val _pendingQuickAdd = MutableStateFlow(false)
    val pendingQuickAdd = _pendingQuickAdd.asStateFlow()

    fun requestQuickAdd() {
        _pendingQuickAdd.value = true
    }

    fun onQuickAddHandled() {
        _pendingQuickAdd.value = false
    }

    // Set when a receipt was shared to the app; handled like quick add, but the add screen
    // opens pre-filled from SharedReceiptRepository.
    private val _pendingSharedReceipt = MutableStateFlow(false)
    val pendingSharedReceipt = _pendingSharedReceipt.asStateFlow()

    fun requestSharedReceipt() {
        _pendingSharedReceipt.value = true
    }

    fun onSharedReceiptHandled() {
        _pendingSharedReceipt.value = false
    }

    // Set when opened from the "automatic backup failed" notification; handled like quick add.
    private val _pendingOpenAutoBackup = MutableStateFlow(false)
    val pendingOpenAutoBackup = _pendingOpenAutoBackup.asStateFlow()

    fun requestOpenAutoBackup() {
        _pendingOpenAutoBackup.value = true
    }

    fun onOpenAutoBackupHandled() {
        _pendingOpenAutoBackup.value = false
    }

    fun onAuthenticationSuccess() {
        _isAuthenticated.value = true
    }

    // elapsedRealtime when the app last left the foreground while unlocked.
    private var backgroundedAt: Long? = null

    /** Called from Activity.onStop (not for configuration changes). */
    fun onAppBackgrounded(now: Long) {
        if (_isAuthenticated.value) {
            backgroundedAt = now
        }
    }

    /**
     * Called from Activity.onStart. Re-locks the app if it stayed in the background longer
     * than [RELOCK_TIMEOUT_MS]. The short grace period keeps quick trips to the camera,
     * gallery or file picker (attachments, backup, export) from asking to unlock again.
     */
    fun onAppForegrounded(now: Long) {
        val leftAt = backgroundedAt ?: return
        backgroundedAt = null
        if (now - leftAt >= RELOCK_TIMEOUT_MS) {
            _isAuthenticated.value = false
        }
    }

    // region app_opened analytics

    // Source of the intent that brought the app up, waiting for the next onResume to report it.
    private var pendingOpenSource: String? = null

    // One report per visit; survives rotation and theme or language changes, which recreate the
    // Activity but aren't new opens. Reset when the app really goes to the background.
    private var isOpenReported = false
    private var stoppedAt: Long? = null

    /** A launch or new intent (notification, widget, shortcut, share, launcher icon). */
    fun onOpenIntent(source: String) {
        pendingOpenSource = source
        isOpenReported = false
    }

    /** Activity.onStop when not just changing configuration. */
    fun onStoppedForOpenTracking(now: Long) {
        isOpenReported = false
        stoppedAt = now
    }

    /**
     * Activity.onResume: the source to report as app_opened, or null when this isn't a new open.
     * Coming back without a new intent counts only after [OPEN_GRACE_MS] away, so returning from
     * the camera, scanner, file picker or share sheet isn't counted as opening the app again.
     */
    fun takeOpenSource(now: Long): String? {
        if (isOpenReported) return null
        isOpenReported = true
        pendingOpenSource?.let {
            pendingOpenSource = null
            stoppedAt = null
            return it
        }
        val leftAt = stoppedAt ?: return AppOpenSource.RESTORED
        stoppedAt = null
        return if (now - leftAt >= OPEN_GRACE_MS) AppOpenSource.RESUME else null
    }

    // endregion

    companion object {
        const val RELOCK_TIMEOUT_MS = 30_000L
        const val OPEN_GRACE_MS = 30_000L
    }
}
