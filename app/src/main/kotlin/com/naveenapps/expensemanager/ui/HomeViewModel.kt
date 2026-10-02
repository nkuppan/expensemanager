package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.reminder.UpdateReminderStatusUseCase
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.notification.NotificationScheduler
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val updateReminderStatusUseCase: UpdateReminderStatusUseCase,
    private val notificationScheduler: NotificationScheduler,
    private val analyticsRepository: AnalyticsRepository,
    private val reminderTimeRepository: ReminderTimeRepository,
    private val feedbackRepository: FeedbackRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _homeScreenBottomBarItems = MutableStateFlow(HomeScreenBottomBarItems.Home)
    val homeScreenBottomBarItems = _homeScreenBottomBarItems.asStateFlow()

    /**
     * True when we should offer the "turn on reminders" explainer: the user has saved at least
     * one transaction (so they've seen the app's value) and hasn't answered the explainer yet.
     * The screen still checks the OS permission, since there's nothing to ask if it's granted.
     */
    val shouldOfferReminderPrimer: StateFlow<Boolean> = combine(
        feedbackRepository.getTransactionCreatedCount(),
        reminderTimeRepository.isNotificationPrimerShown(),
    ) { transactionCount, primerShown ->
        transactionCount >= 1 && !primerShown
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** One-shot celebration after the install's very first saved transaction. */
    val showFirstSaveCelebration: StateFlow<Boolean> = feedbackRepository
        .isFirstSaveCelebrationPending()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun onFirstSaveCelebrationShown() {
        analyticsRepository.logEvent(AnalyticsEvents.FIRST_SAVE_CELEBRATION_SHOWN, emptyMap())
    }

    /** "Add another": keep the momentum going, straight back to the keypad. */
    fun onFirstSaveAddAnother() {
        analyticsRepository.logEvent(AnalyticsEvents.FIRST_SAVE_ADD_ANOTHER, emptyMap())
        clearFirstSaveCelebration()
        openAddTransaction()
    }

    fun onFirstSaveDone() {
        analyticsRepository.logEvent(AnalyticsEvents.FIRST_SAVE_DONE, emptyMap())
        clearFirstSaveCelebration()
    }

    private fun clearFirstSaveCelebration() {
        viewModelScope.launch { feedbackRepository.setFirstSaveCelebrationPending(false) }
    }

    // Bottom-bar tabs aren't navigation destinations, so log them as their own screens
    // (HomeAnalysis, HomeTransaction, ...). The landing tab is covered by the "Home" view.
    private fun screenName(item: HomeScreenBottomBarItems) = "Home${item.name}"

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        if (homeScreenBottomBarItems != _homeScreenBottomBarItems.value) {
            analyticsRepository.setCurrentScreen(screenName(homeScreenBottomBarItems))
        }
        _homeScreenBottomBarItems.value = homeScreenBottomBarItems
    }

    /** Centre "+" in the bottom bar: same new-transaction screen from every tab. */
    fun openAddTransaction() {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
    }

    fun onReminderPrimerShown() {
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_SHOWN, emptyMap())
    }

    /** User tapped "Turn on reminders": the screen launches the system dialog next. */
    fun onReminderPrimerAccepted() {
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_ACCEPTED, emptyMap())
        markPrimerAnswered()
    }

    /** "Not now" or swiped away: respect it and never show the explainer again. */
    fun onReminderPrimerDismissed() {
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_DISMISSED, emptyMap())
        markPrimerAnswered()
    }

    private fun markPrimerAnswered() {
        viewModelScope.launch {
            reminderTimeRepository.setNotificationPrimerShown()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        analyticsRepository.logEvent(
            AnalyticsEvents.NOTIFICATION_PERMISSION_RESULT,
            mapOf(AnalyticsParams.GRANTED to granted.toString()),
        )
    }

    /**
     * Reschedules the daily reminder only if the user still has it switched on (and we're
     * allowed to notify). checkAndRestartReminder() does both checks itself.
     */
    fun restartReminderIfEnabled() {
        viewModelScope.launch {
            notificationScheduler.checkAndRestartReminder()
        }
    }

    fun turnOnNotification() {
        viewModelScope.launch {
            updateReminderStatusUseCase.invoke(true)
            notificationScheduler.checkAndRestartReminder()
        }
    }
}
