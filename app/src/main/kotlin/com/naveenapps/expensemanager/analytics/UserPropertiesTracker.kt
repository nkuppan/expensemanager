package com.naveenapps.expensemanager.analytics

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.widget.ExpenseWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Keeps Analytics user properties in step with how the user uses the app, so every event can
 * be split by behaviour: e.g. retention of users with the reminder on vs off, or the Add-screen
 * drop for people with one account vs several. Values are coarse buckets and choices, never
 * amounts or names. Started once per process; [refresh] re-reads what has no stream (the
 * notification permission, placed widgets, app language) when the app comes to the front.
 */
class UserPropertiesTracker(
    private val context: Context,
    private val analyticsRepository: AnalyticsRepository,
    private val feedbackRepository: FeedbackRepository,
    private val reminderTimeRepository: ReminderTimeRepository,
    private val budgetRepository: BudgetRepository,
    private val accountRepository: AccountRepository,
    private val themeRepository: ThemeRepository,
    private val currencyRepository: CurrencyRepository,
) {

    private val refreshTick = MutableStateFlow(0)

    fun start(scope: CoroutineScope) {
        feedbackRepository.getTransactionCreatedCount()
            .map { transactionsBucket(it) }
            .distinctUntilChanged()
            .onEach { set(TRANSACTIONS_LOGGED, it) }
            .catch { Log.w(TAG, "transactions_logged", it) }
            .launchIn(scope)

        // On only when it can actually arrive: switched on and notifications allowed.
        combine(reminderTimeRepository.isReminderOn(), refreshTick) { on, _ ->
            on && NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
            .distinctUntilChanged()
            .onEach { set(REMINDER_ON, it.toString()) }
            .catch { Log.w(TAG, "reminder_on", it) }
            .launchIn(scope)

        budgetRepository.getBudgets()
            .map { it.isNotEmpty() }
            .distinctUntilChanged()
            .onEach { set(HAS_BUDGET, it.toString()) }
            .catch { Log.w(TAG, "has_budget", it) }
            .launchIn(scope)

        accountRepository.getAccounts()
            .map { accountsBucket(it.size) }
            .distinctUntilChanged()
            .onEach { set(ACCOUNT_COUNT, it) }
            .catch { Log.w(TAG, "account_count", it) }
            .launchIn(scope)

        themeRepository.getSelectedTheme()
            .map { themeName(it.mode) }
            .distinctUntilChanged()
            .onEach { set(APP_THEME, it) }
            .catch { Log.w(TAG, "app_theme", it) }
            .launchIn(scope)

        currencyRepository.getSelectedCurrency()
            .map { it.code.ifBlank { it.name } }
            .distinctUntilChanged()
            .onEach { set(CURRENCY_CODE, it) }
            .catch { Log.w(TAG, "currency_code", it) }
            .launchIn(scope)

        refreshTick
            .onEach {
                val widgets = runCatching {
                    GlanceAppWidgetManager(context).getGlanceIds(ExpenseWidget::class.java).isNotEmpty()
                }.getOrNull()
                widgets?.let { set(WIDGET_PLACED, it.toString()) }
                set(APP_LANGUAGE, appLanguage())
            }
            .catch { Log.w(TAG, "refresh", it) }
            .launchIn(scope)
    }

    /** The app came to the front: re-read the permission, widgets and language. */
    fun refresh() {
        refreshTick.value++
    }

    private fun set(name: String, value: String) {
        analyticsRepository.setUserProperty(name, value)
    }

    // The language chosen in the app, or "system" when it follows the phone.
    private fun appLanguage(): String = AppCompatDelegate.getApplicationLocales()
        .get(0)?.language?.takeIf { it.isNotBlank() } ?: LANGUAGE_SYSTEM

    companion object {
        private const val TAG = "UserProperties"

        const val TRANSACTIONS_LOGGED = "transactions_logged"
        const val REMINDER_ON = "reminder_on"
        const val HAS_BUDGET = "has_budget"
        const val WIDGET_PLACED = "widget_placed"
        const val ACCOUNT_COUNT = "accounts"
        const val APP_THEME = "app_theme"
        const val APP_LANGUAGE = "app_language"
        const val CURRENCY_CODE = "currency_code"

        private const val LANGUAGE_SYSTEM = "system"

        /** Transactions saved on this install, in buckets: 0, 1, 2-5, 6-20, 21-100, 100+. */
        fun transactionsBucket(count: Int): String = when {
            count <= 0 -> "0"
            count == 1 -> "1"
            count <= 5 -> "2-5"
            count <= 20 -> "6-20"
            count <= 100 -> "21-100"
            else -> "100+"
        }

        /** Accounts: 1, 2, 3, 4, 5+ (0 only before setup). */
        fun accountsBucket(count: Int): String = if (count >= 5) "5+" else count.coerceAtLeast(0).toString()

        fun themeName(mode: Int): String = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "light"
            AppCompatDelegate.MODE_NIGHT_YES -> "dark"
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> "battery"
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "system"
            else -> "other"
        }
    }
}
