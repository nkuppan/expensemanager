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

    fun onAuthenticationSuccess() {
        _isAuthenticated.value = true
    }
}
