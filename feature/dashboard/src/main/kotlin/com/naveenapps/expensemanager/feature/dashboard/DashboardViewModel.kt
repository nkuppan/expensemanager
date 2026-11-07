package com.naveenapps.expensemanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.AppShareLink
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.GetUpcomingRecurringUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.needsAttention
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.feature.budget.components.toBudgetProgress
import com.naveenapps.expensemanager.feature.dashboard.notifications.LATEST_WHATS_NEW_VERSION
import java.time.LocalDate
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    getBudgetsUseCase: GetBudgetsUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    getDateRangeUseCase: GetDateRangeUseCase,
    settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val feedbackRepository: FeedbackRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val autoBackupRepository: AutoBackupRepository,
    getUpcomingRecurringUseCase: GetUpcomingRecurringUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            expenseFlowState = ExpenseFlowState(),
            transactions = emptyList(),
            accounts = emptyList(),
            categoryTransactionState = CategoryTransactionState(
                pieChartData = listOf(),
                totalAmount = Amount(0.0),
                categoryTransactions = emptyList(),
                categoryType = CategoryType.EXPENSE,
            ),
            transactionPeriod = "",
        ),
    )
    val state = _state.asStateFlow()

    init {
        // Lifetime "transactions created" count (the same one behind analytics' is_first), so
        // an empty current month isn't mistaken for a brand-new user.
        feedbackRepository.getTransactionCreatedCount()
            .onEach { count ->
                _state.update { it.copy(hasCreatedTransaction = count > 0, transactionCount = count) }
            }
            .launchIn(viewModelScope)

        // "Automatic backup failed" banner. The snooze time is checked when settings change;
        // a snooze that ran out shows the banner again next time Home is created.
        autoBackupRepository.getSettings()
            .onEach { settings ->
                val warning = settings.lastError.takeIf { settings.needsAttention(System.currentTimeMillis()) }
                _state.update { it.copy(backupWarning = warning) }
            }
            .launchIn(viewModelScope)

        // "What's new" dot. A fresh install has nothing "new" to read, so its notes start as
        // seen; anyone who updated sees the dot until they open the notes once.
        viewModelScope.launch {
            if (feedbackRepository.getLastSeenWhatsNew().first() == null && feedbackRepository.isFreshInstall()) {
                feedbackRepository.setLastSeenWhatsNew(LATEST_WHATS_NEW_VERSION)
            }
            // Dot on the bell: unread release notes, or an insight posted since the last visit.
            combine(
                feedbackRepository.getLastSeenWhatsNew(),
                feedbackRepository.getInsights(),
                feedbackRepository.getNotificationCentreOpenedAt(),
            ) { seenRelease, insights, openedAt ->
                seenRelease != LATEST_WHATS_NEW_VERSION || insights.any { it.postedAt > openedAt }
            }
                .onEach { unread -> _state.update { it.copy(hasUnreadNotifications = unread) } }
                .launchIn(viewModelScope)
        }

        settingsRepository.getDashboardOrder()
            .onEach { order -> _state.update { it.copy(sectionOrder = order) } }
            .launchIn(viewModelScope)

        feedbackRepository.getShareBannerHiddenUntil()
            .onEach { until -> _state.update { it.copy(shareBannerHiddenUntil = until) } }
            .launchIn(viewModelScope)

        // Inputs for the "Get started" checklist.
        feedbackRepository.isGettingStartedDismissed()
            .onEach { dismissed -> _state.update { it.copy(isGettingStartedDismissed = dismissed) } }
            .launchIn(viewModelScope)

        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
            getDateRangeUseCase.invoke(),
        ) { currency, transactions, accounts, dateRange ->

            val filteredTransactions = (
                transactions?.map {
                    it.toTransactionUIModel(
                        getFormattedAmountUseCase.invoke(
                            it.amount.amount,
                            currency,
                        ),
                    )
                } ?: emptyList()
                ).take(MAX_TRANSACTIONS_IN_LIST)

            val accountsConverted = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency,
                    ),
                    if (it.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase.invoke(
                            it.getAvailableCreditLimit(),
                            currency,
                        )
                    } else {
                        null
                    },
                )
            }

            val incomeValue = transactions?.filter { it.type == TransactionType.INCOME }?.sumOf {
                it.amount.amount
            } ?: 0.0

            val expenseValue = transactions?.filter { it.type == TransactionType.EXPENSE }?.sumOf {
                it.amount.amount
            } ?: 0.0

            _state.update {
                it.copy(
                    expenseFlowState = it.expenseFlowState.copy(
                        income = getFormattedAmountUseCase.invoke(
                            incomeValue,
                            currency,
                        ).amountString.orEmpty(),
                        expense = getFormattedAmountUseCase.invoke(
                            expenseValue,
                            currency,
                        ).amountString.orEmpty(),
                        balance = getFormattedAmountUseCase.invoke(
                            (incomeValue - expenseValue),
                            currency,
                        ).amountString.orEmpty(),
                    ),
                    transactions = filteredTransactions,
                    accounts = accountsConverted,
                    transactionPeriod = if (dateRange.description.isNotEmpty()) {
                        "${dateRange.name} (${dateRange.description})"
                    } else {
                        dateRange.name
                    },
                )
            }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        // Full lists: the categories card shows the top few and groups the rest into one slice.
        getTransactionGroupByCategoryUseCase.invoke(CategoryType.EXPENSE).onEach { expense ->
            _state.update { it.copy(categoryTransactionState = expense) }
        }.launchIn(viewModelScope)

        getTransactionGroupByCategoryUseCase.invoke(CategoryType.INCOME).onEach { income ->
            _state.update { it.copy(incomeCategoryTransactionState = income) }
        }.launchIn(viewModelScope)

        combine(
            getBudgetsUseCase.invoke(),
            getDateRangeUseCase.invoke(),
            getCurrencyUseCase.invoke(),
        ) { allBudgets, dateRange, currency ->
            val activeMonthDate = when (dateRange.type) {
                DateRangeType.TODAY, DateRangeType.THIS_WEEK, DateRangeType.THIS_MONTH ->
                    Date(dateRange.dateRanges[0])

                else -> Date()
            }
            val activeMonth = activeMonthDate.toMonthAndYearKey()
            val currentYear = Date().toYear()
            // Home shows every monthly budget for the month being viewed and every yearly budget
            // for this year, one card each.
            val (monthly, yearly) = pickHomeBudgets(allBudgets, activeMonth, currentYear)
            val today = LocalDate.now()
            val format = { value: Double -> getFormattedAmountUseCase.invoke(value, currency).amountString.orEmpty() }
            _state.update {
                it.copy(
                    homeBudgets = HomeBudgets(
                        monthly = monthly.mapNotNull { budget -> budget.toBudgetProgress(today, format) },
                        yearly = yearly.mapNotNull { budget -> budget.toBudgetProgress(today, format) },
                        month = activeMonthDate,
                        year = currentYear,
                    ),
                    // "Create a monthly budget" is done only by a monthly budget for the current
                    // calendar month — what Home shows — not by any old or restored budget.
                    hasCurrentMonthBudget = allBudgets.any {
                        it.periodType == BudgetPeriod.MONTHLY &&
                            it.selectedMonth == Date().toMonthAndYearKey()
                    },
                )
            }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        // Upcoming card. Rules and their totals don't depend on the selected period or filters.
        combine(
            getUpcomingRecurringUseCase.invoke(),
            getCurrencyUseCase.invoke(),
        ) { upcoming, currency ->
            val format = { value: Double -> getFormattedAmountUseCase.invoke(value, currency).amountString.orEmpty() }
            UpcomingUi(
                items = upcoming.rules.take(MAX_UPCOMING_IN_LIST).map { UpcomingItemUi(it, format(it.amount)) },
                expenseDue = upcoming.expenseDue.takeIf { it > 0.0 }?.let(format),
                incomeDue = upcoming.incomeDue.takeIf { it > 0.0 }?.let(format),
                windowDays = upcoming.windowDays,
            )
        }.onEach { upcoming ->
            _state.update { it.copy(upcoming = upcoming) }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        settingsRepository.getHomeSummaryCompact().onEach { compact ->
            _state.update { it.copy(isCompactSummary = compact) }
        }.launchIn(viewModelScope)
    }

    private fun openSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Settings)
    }

    private fun openAccountList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountList)
    }

    private fun openAccountCreate(accountId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountCreate(accountId))
    }

    private fun openBudgetList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetList)
    }

    private fun openBudgetCreate(period: BudgetPeriod? = null) {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetCreate(null, period?.name))
    }

    private fun openBudgetDetails(budgetId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetDetails(budgetId))
    }

    private fun openTransactionList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionList)
    }

    private fun openTransactionCreate(transactionId: String? = null) {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(transactionId))
    }

    // share_banner_shown is logged once per visit to Home, not on every recomposition.
    private var shareBannerLogged = false

    fun processAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OpenAccountEdit -> openAccountCreate(action.account.id)

            DashboardAction.OpenAccountList -> openAccountList()

            is DashboardAction.OpenBudgetDetails -> openBudgetDetails(action.id)

            is DashboardAction.EditBudget -> appComposeNavigator.navigate(ExpenseManagerScreens.BudgetCreate(action.id))

            DashboardAction.OpenBudgetList -> openBudgetList()

            is DashboardAction.OpenBudgetCreate -> openBudgetCreate(action.period)

            DashboardAction.OpenSettings -> openSettings()

            is DashboardAction.OpenTransactionEdit -> openTransactionCreate(action.transaction?.id)

            DashboardAction.OpenTransactionList -> openTransactionList()

            DashboardAction.DismissGettingStarted -> viewModelScope.launch {
                feedbackRepository.setGettingStartedDismissed()
            }

            DashboardAction.FixAutoBackup -> {
                analyticsRepository.logEvent(
                    AnalyticsEvents.AUTO_BACKUP_FIX_OPENED,
                    mapOf(AnalyticsParams.SOURCE to "home_banner"),
                )
                appComposeNavigator.navigate(ExpenseManagerScreens.AutoBackupScreen)
            }

            DashboardAction.OpenNotifications -> {
                analyticsRepository.logEvent(
                    AnalyticsEvents.WHATS_NEW_OPENED,
                    mapOf(AnalyticsParams.SOURCE to if (_state.value.hasUnreadNotifications) "unread" else "read"),
                )
                // The centre marks everything as read when it opens.
                appComposeNavigator.navigate(ExpenseManagerScreens.NotificationCentre)
            }

            DashboardAction.OpenCustomiseDashboard ->
                appComposeNavigator.navigate(ExpenseManagerScreens.DashboardReOrder)

            DashboardAction.ShareBannerShown -> if (!shareBannerLogged) {
                shareBannerLogged = true
                analyticsRepository.logEvent(AnalyticsEvents.SHARE_BANNER_SHOWN, emptyMap())
            }

            DashboardAction.ShareAppFromBanner -> viewModelScope.launch {
                analyticsRepository.logEvent(
                    AnalyticsEvents.SHARE_APP,
                    mapOf(AnalyticsParams.SOURCE to AppShareLink.MEDIUM_HOME_BANNER),
                )
                // Asked once and they shared: don't ask again.
                feedbackRepository.hideShareBanner(Long.MAX_VALUE)
            }

            DashboardAction.DismissShareBanner -> viewModelScope.launch {
                analyticsRepository.logEvent(AnalyticsEvents.SHARE_BANNER_DISMISSED, emptyMap())
                feedbackRepository.hideShareBanner(
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(SHARE_BANNER_SNOOZE_DAYS),
                )
            }

            DashboardAction.OpenRecurringList ->
                appComposeNavigator.navigate(ExpenseManagerScreens.RecurringTransactions)

            is DashboardAction.OpenRecurringEdit ->
                appComposeNavigator.navigate(ExpenseManagerScreens.RecurringTransactionEdit(action.id))

            DashboardAction.PlanRecurring -> appComposeNavigator.navigate(
                ExpenseManagerScreens.TransactionCreate(id = null, repeat = RecurringFrequency.MONTHLY.name),
            )

            DashboardAction.SnoozeAutoBackupWarning -> viewModelScope.launch {
                autoBackupRepository.snoozeFailureWarning(
                    System.currentTimeMillis() + AutoBackupRepository.WARNING_SNOOZE_MILLIS,
                )
            }
        }
    }

    companion object {
        private const val MAX_TRANSACTIONS_IN_LIST = 10
        private const val MAX_UPCOMING_IN_LIST = 3
    }
}
