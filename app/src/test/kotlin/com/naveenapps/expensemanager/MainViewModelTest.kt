package com.naveenapps.expensemanager

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelTest : BaseCoroutineTest() {

    private val settingsRepository: SettingsRepository = mock {
        whenever(it.isAppLockEnabled()).thenReturn(flowOf(true))
        whenever(it.isOnboardingCompleted()).thenReturn(flowOf(true))
    }
    private val themeRepository: ThemeRepository = mock {
        whenever(it.getSelectedTheme()).thenReturn(emptyFlow())
    }

    private fun viewModel() = MainViewModel(
        getCurrentThemeUseCase = GetCurrentThemeUseCase(themeRepository),
        getOnboardingStatusUseCase = GetOnboardingStatusUseCase(settingsRepository),
        settingsRepository = settingsRepository,
        analyticsRepository = mock<AnalyticsRepository>(),
    )

    @Test
    fun staysUnlockedAfterShortTripToBackground() {
        val viewModel = viewModel()
        viewModel.onAuthenticationSuccess()

        viewModel.onAppBackgrounded(now = 1_000L)
        viewModel.onAppForegrounded(now = 1_000L + MainViewModel.RELOCK_TIMEOUT_MS - 1)

        Truth.assertThat(viewModel.isAuthenticated.value).isTrue()
    }

    @Test
    fun relocksAfterTimeoutInBackground() {
        val viewModel = viewModel()
        viewModel.onAuthenticationSuccess()

        viewModel.onAppBackgrounded(now = 1_000L)
        viewModel.onAppForegrounded(now = 1_000L + MainViewModel.RELOCK_TIMEOUT_MS)

        Truth.assertThat(viewModel.isAuthenticated.value).isFalse()
    }

    @Test
    fun foregroundWithoutBackgroundDoesNothing() {
        val viewModel = viewModel()
        viewModel.onAuthenticationSuccess()

        viewModel.onAppForegrounded(now = Long.MAX_VALUE)

        Truth.assertThat(viewModel.isAuthenticated.value).isTrue()
    }

    @Test
    fun backgroundWhileLockedDoesNotArmTimer() {
        val viewModel = viewModel()

        // e.g. leaving for the system PIN screen while the lock is showing
        viewModel.onAppBackgrounded(now = 0L)
        viewModel.onAuthenticationSuccess()
        viewModel.onAppForegrounded(now = MainViewModel.RELOCK_TIMEOUT_MS * 10)

        Truth.assertThat(viewModel.isAuthenticated.value).isTrue()
    }
}
