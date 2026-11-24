package com.naveenapps.expensemanager.feature.analysis

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAverageDataUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetChartDataUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetPeriodComparisonUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetSpendingInsightsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.toTransactionUIModel
import com.naveenapps.expensemanager.core.foundation.toDayKey
import com.naveenapps.expensemanager.core.foundation.toYearKey
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDate

class AnalysisScreenViewModel(
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getChartDataUseCase: GetChartDataUseCase,
    getAverageDataUseCase: GetAverageDataUseCase,
    getAmountStateUseCase: GetAmountStateUseCase,
    getDateRangeUseCase: GetDateRangeUseCase,
    settingsRepository: SettingsRepository,
    getSpendingInsightsUseCase: GetSpendingInsightsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getPeriodComparisonUseCase: GetPeriodComparisonUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val analyticsRepository: AnalyticsRepository,
) : ViewModel() {

    private val _insights = MutableStateFlow<AnalysisInsightsUi?>(null)
    val insights = _insights.asStateFlow()

    private val _comparison = MutableStateFlow<PeriodComparisonUi?>(null)
    val comparison = _comparison.asStateFlow()

    private val _currentTheme = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            R.string.analysis,
        ),
    )
    val currentTheme = _currentTheme.asStateFlow()

    private val _expenseFlowState = MutableStateFlow(ExpenseFlowState())
    val amountUiState = _expenseFlowState.asStateFlow()

    private val _transactionPeriod = MutableStateFlow("")
    val transactionPeriod = _transactionPeriod.asStateFlow()

    private val _isCompactSummary = MutableStateFlow(false)
    val isCompactSummary = _isCompactSummary.asStateFlow()

    private val _graphItems = MutableStateFlow<AnalysisUiData?>(null)
    val graphItems = _graphItems.asStateFlow()

    private val _averageData = MutableStateFlow(
        WholeAverageData(
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            ),
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            ),
        ),
    )
    val averageData = _averageData.asStateFlow()

    init {
        combine(
            getSpendingInsightsUseCase.invoke(),
            getCurrencyUseCase.invoke(),
        ) { insights, currency ->
            val format: (Double) -> String = {
                getFormattedAmountUseCase.invoke(it, currency).amountString.orEmpty()
            }
            val maxWeekday = insights.weekdays.maxOfOrNull { it.total } ?: 0.0
            AnalysisInsightsUi(
                hasExpenses = insights.totalExpense > 0,
                savingsRate = insights.savingsRate,
                categories = insights.categories.map {
                    CategoryRowUi(it.category, format(it.total), it.share, it.count)
                },
                weekdays = insights.weekdays.map {
                    WeekdayBarUi(
                        dayOfWeek = it.dayOfWeek,
                        amount = format(it.total),
                        relative = if (maxWeekday > 0) (it.total / maxWeekday).toFloat() else 0f,
                    )
                },
                busiestWeekday = insights.busiestWeekday,
                busiestWeekdayShare = if (insights.totalExpense > 0) {
                    (maxWeekday / insights.totalExpense).toFloat()
                } else {
                    0f
                },
                biggestExpenses = insights.biggestExpenses.map {
                    it.toTransactionUIModel(getFormattedAmountUseCase.invoke(it.amount.amount, currency))
                },
            )
        }.onEach { _insights.value = it }.launchIn(viewModelScope)

        combine(
            getPeriodComparisonUseCase.invoke(),
            getCurrencyUseCase.invoke(),
        ) { comparison, currency ->
            comparison?.toUi { getFormattedAmountUseCase.invoke(it, currency).amountString.orEmpty() }
        }.onEach { _comparison.value = it }.launchIn(viewModelScope)

        getChartDataUseCase.invoke().onEach { response ->
            _graphItems.value = AnalysisUiData(
                transactions = response.transactions,
                chartData = response.chartData?.let { chart ->
                    AnalysisUiChartData(
                        expenses = chart.chartData.getOrNull(0).orEmpty().map { it.total },
                        incomes = chart.chartData.getOrNull(1).orEmpty().map { it.total },
                        dates = chart.dates.map { chartLabel(chart.groupType, it) },
                    ).takeIf { it.expenses.isNotEmpty() }
                },
            )
        }.launchIn(viewModelScope)

        getAverageDataUseCase.invoke().onEach { response ->
            _averageData.value = response
        }.launchIn(viewModelScope)

        getAmountStateUseCase.invoke().onEach { response ->
            _expenseFlowState.value = response
        }.launchIn(viewModelScope)

        getCurrentThemeUseCase.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)

        getDateRangeUseCase.invoke().onEach {
            _transactionPeriod.value = if (it.description.isNotEmpty()) {
                "${it.name} (${it.description})"
            } else {
                it.name
            }
        }.launchIn(viewModelScope)

        settingsRepository.getHomeSummaryCompact().onEach {
            _isCompactSummary.value = it
        }.launchIn(viewModelScope)
    }

    fun openCategory(categoryId: String) {
        logDrillDown(DRILL_CATEGORY)
        appComposeNavigator.navigate(ExpenseManagerScreens.CategoryDetails(categoryId))
    }

    fun openCategoryList() {
        logDrillDown(DRILL_CATEGORY_LIST)
        appComposeNavigator.navigate(ExpenseManagerScreens.CategoryTransaction)
    }

    fun openTransaction(transactionId: String) {
        logDrillDown(DRILL_TRANSACTION)
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(transactionId))
    }

    /** What people open from Analysis: a category, all categories or a transaction. */
    private fun logDrillDown(target: String) {
        analyticsRepository.logEvent(AnalyticsEvents.ANALYSIS_DRILL_DOWN, mapOf(AnalyticsParams.TYPE to target))
    }

    private companion object {
        const val DRILL_CATEGORY = "category"
        const val DRILL_CATEGORY_LIST = "category_list"
        const val DRILL_TRANSACTION = "transaction"
    }
}

data class AnalysisUiData(
    val transactions: List<TransactionUiItem>,
    val chartData: AnalysisUiChartData? = null,
)

/** One value per date bucket: index i of [expenses], [incomes] and [dates] is the same day. */
data class AnalysisUiChartData(
    val expenses: List<Double>,
    val incomes: List<Double>,
    val dates: List<String>,
    val title: String? = null,
)

/** The label of a chart bucket starting on [date]: the year, the month in the app's language, or the day. */
internal fun chartLabel(groupType: GroupType, date: LocalDate): String = when (groupType) {
    GroupType.YEAR -> date.toYearKey()
    GroupType.MONTH -> date.toMonthAndYear()
    GroupType.DATE -> date.toDayKey()
}
