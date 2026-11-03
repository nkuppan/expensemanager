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
import kotlinx.coroutines.flow.map
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

    // The situation the explainer was shown in, kept for its answer and the permission result
    // (answering clears the first-save flag, so it can't be worked out again afterwards).
    private var shownSource: String? = null

    /**
     * The "turn on reminders" explainer (setup no longer asks) is offered once, right after the
     * install's first saved expense, while the value is fresh. Most users are on Android 13+,
     * where this is the only way to get a reminder on the first evening, the moment most likely
     * to bring a new user back. It stays offered until answered; reminders can also be turned
     * on any time in Settings. The screen still checks the OS permission, since there's nothing
     * to ask if it's granted.
     */
    private val reminderPrimer: StateFlow<ReminderPrimerOffer> = combine(
        reminderTimeRepository.isNotificationPrimerShown(),
        feedbackRepository.isFirstSaveCelebrationPending(),
    ) { primerShown, firstSavePending ->
        ReminderPrimerOffer(
            show = !primerShown && firstSavePending,
            source = PRIMER_SOURCE_FIRST_SAVE,
            firstSavePending = firstSavePending,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderPrimerOffer())

    val shouldOfferReminderPrimer: StateFlow<Boolean> = reminderPrimer
        .map { it.show }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Set by the first saved expense until Home has asked about reminders (or had no need to). */
    val firstSavePending: StateFlow<Boolean> = reminderPrimer
        .map { it.firstSavePending }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Which situation the explainer was offered in, so grant rates can be compared per trigger. */
    private fun primerSource(): Map<String, String> =
        mapOf(AnalyticsParams.SOURCE to (shownSource ?: reminderPrimer.value.source))

    /** Notifications are already allowed, so there's nothing to ask after the first expense. */
    fun onFirstSaveNothingToAsk() {
        clearFirstSavePending()
    }

    private fun clearFirstSavePending() {
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
        shownSource = reminderPrimer.value.source
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_SHOWN, primerSource())
    }

    /** User tapped "Turn on reminders": the screen launches the system dialog next. */
    fun onReminderPrimerAccepted() {
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_ACCEPTED, primerSource())
        markPrimerAnswered()
    }

    /** "Not now" or swiped away: respect it and never show the explainer again. */
    fun onReminderPrimerDismissed() {
        analyticsRepository.logEvent(AnalyticsEvents.NOTIFICATION_PRIMER_DISMISSED, primerSource())
        markPrimerAnswered()
    }

    private fun markPrimerAnswered() {
        viewModelScope.launch {
            reminderTimeRepository.setNotificationPrimerShown()
            feedbackRepository.setFirstSaveCelebrationPending(false)
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        analyticsRepository.logEvent(
            AnalyticsEvents.NOTIFICATION_PERMISSION_RESULT,
            mapOf(AnalyticsParams.GRANTED to granted.toString()) + primerSource(),
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

private data class ReminderPrimerOffer(
    val show: Boolean = false,
    val source: String = PRIMER_SOURCE_FIRST_SAVE,
    val firstSavePending: Boolean = false,
)

private const val PRIMER_SOURCE_FIRST_SAVE = "first_save"
