package com.naveenapps.expensemanager.core.datastore

import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Every settings store reads back what it wrote, and its defaults when nothing was written,
 * through a real file-backed DataStore, on the JVM and on iOS (OpenSpec change
 * make-core-datastore-multiplatform, design D5).
 */
class StoresRoundTripTest {

    @Test
    fun themeLocaleAndCurrency() = runTest {
        val dataStore = testDataStore()
        val theme = ThemeDataStore(dataStore)
        val locale = LocaleDataStore(dataStore)
        val currency = CurrencyDataStore(dataStore)
        val defaultCurrency = Currency(symbol = "$", name = "US Dollar", code = "USD")

        assertEquals(-1, theme.getTheme(-1).first())
        assertEquals("system", locale.getLocaleTag("system").first())
        assertEquals(defaultCurrency, currency.getCurrency(defaultCurrency).first())
        assertFalse(currency.hasCurrency().first())

        theme.setTheme(2)
        locale.setLocaleTag("de")
        currency.setCurrency(name = "Indian Rupee", symbol = "₹", code = "INR", position = CurrencyPosition.SUFFIX.ordinal)

        assertEquals(2, theme.getTheme(-1).first())
        assertEquals("de", locale.getLocaleTag("system").first())
        assertEquals(
            Currency(symbol = "₹", name = "Indian Rupee", position = CurrencyPosition.SUFFIX, code = "INR"),
            currency.getCurrency(defaultCurrency).first(),
        )
        assertTrue(currency.hasCurrency().first())
    }

    @Test
    fun reminders() = runTest {
        val reminder = ReminderTimeDataStore(testDataStore())

        assertEquals(ReminderTimeState(hour = 10, minute = 0, is24Hour = false), reminder.getReminderTime("10:0:false").first())
        assertTrue(reminder.isReminderOn().first())
        assertTrue(reminder.isUpcomingAlertOn().first())
        assertNull(reminder.getLastUpcomingAlertDay().first())
        assertNull(reminder.getReminderScheduledFor().first())
        assertNull(reminder.getReminderLastRunAt().first())

        reminder.setReminderTime(ReminderTimeState(hour = 21, minute = 45, is24Hour = true))
        reminder.setReminder(false)
        reminder.setUpcomingAlertOn(false)
        reminder.setLastUpcomingAlertDay(20_420)
        reminder.setReminderScheduledFor(1_795_000_000_000)
        reminder.setReminderLastRunAt(1_794_000_000_000)

        assertEquals(ReminderTimeState(hour = 21, minute = 45, is24Hour = true), reminder.getReminderTime("10:0:false").first())
        assertFalse(reminder.isReminderOn().first())
        assertFalse(reminder.isUpcomingAlertOn().first())
        assertEquals(20_420L, reminder.getLastUpcomingAlertDay().first())
        assertEquals(1_795_000_000_000L, reminder.getReminderScheduledFor().first())
        assertEquals(1_794_000_000_000L, reminder.getReminderLastRunAt().first())
    }

    @Test
    fun dateRange() = runTest {
        val dateRange = DateRangeDataStore(testDataStore())

        assertEquals(DateRangeType.THIS_MONTH, dateRange.getFilterType().first())
        assertNull(dateRange.getDateRanges().first())

        // Midday, so no DST change is near: stored as local-as-UTC, read back as the same instants.
        val zone = TimeZone.currentSystemDefault()
        val start = LocalDateTime(2026, 3, 1, 12, 0).toInstant(zone).toEpochMilliseconds()
        val end = LocalDateTime(2026, 3, 31, 12, 0).toInstant(zone).toEpochMilliseconds()
        dateRange.setFilterType(DateRangeType.CUSTOM)
        dateRange.setDateRanges(start, end)

        assertEquals(DateRangeType.CUSTOM, dateRange.getFilterType().first())
        assertEquals(listOf(start, end), dateRange.getDateRanges().first())
    }

    @Test
    fun filterSelectionsAndDefaults() = runTest {
        val settings = SettingsDataStore(testDataStore())

        assertEquals(emptyList<TransactionType>(), settings.getTransactionType().first())
        assertEquals(emptyList<String>(), settings.getAccounts().first())
        assertEquals(emptyList<String>(), settings.getCategories().first())
        assertFalse(settings.isPreloaded().first())
        assertFalse(settings.isOnboardingCompleted().first())
        assertNull(settings.getDefaultAccount().first())
        assertNull(settings.getDefaultExpenseCategory().first())
        assertNull(settings.getDefaultIncomeCategory().first())

        settings.setTransactionType(listOf(TransactionType.EXPENSE, TransactionType.INCOME))
        settings.setAccounts(listOf("cash", "bank"))
        settings.setCategories(listOf("food"))
        settings.setPreloaded(true)
        settings.setOnboardingCompleted(true)
        settings.setDefaultAccount("cash")
        settings.setDefaultExpenseCategory("food")
        settings.setDefaultIncomeCategory(null)

        // Stored as string sets, so the order isn't kept.
        assertEquals(setOf(TransactionType.EXPENSE, TransactionType.INCOME), settings.getTransactionType().first()?.toSet())
        assertEquals(setOf("cash", "bank"), settings.getAccounts().first()?.toSet())
        assertEquals(listOf("food"), settings.getCategories().first())
        assertTrue(settings.isPreloaded().first())
        assertTrue(settings.isOnboardingCompleted().first())
        assertEquals("cash", settings.getDefaultAccount().first())
        assertEquals("food", settings.getDefaultExpenseCategory().first())
        assertEquals("", settings.getDefaultIncomeCategory().first())

        settings.setTransactionType(null)
        assertEquals(emptyList<TransactionType>(), settings.getTransactionType().first())
    }

    @Test
    fun deviceLocalAndAutoBackup() = runTest {
        val deviceLocal = testDataStore()
        val primer = DeviceLocalDataStore(deviceLocal)
        val autoBackup = AutoBackupDataStore(deviceLocal)

        assertFalse(primer.isNotificationPrimerShown().first())
        primer.setNotificationPrimerShown()
        assertTrue(primer.isNotificationPrimerShown().first())

        val defaults = autoBackup.getSettings().first()
        assertFalse(defaults.isEnabled)
        assertNull(defaults.folderUri)
        assertEquals(AutoBackupFrequency.DAILY, defaults.frequency)
        assertNull(defaults.lastBackupAt)
        assertNull(defaults.lastError)
        assertFalse(defaults.isFailureNotified)

        autoBackup.setEnabled(true)
        autoBackup.setFrequency(AutoBackupFrequency.WEEKLY)
        autoBackup.setFolder("content://tree/backups", "Backups")
        autoBackup.recordError(AutoBackupError.FOLDER_UNAVAILABLE)
        autoBackup.snoozeWarning(1_795_000_000_000)
        autoBackup.setFailureNotified()

        val failing = autoBackup.getSettings().first()
        assertTrue(failing.isEnabled)
        assertEquals(AutoBackupFrequency.WEEKLY, failing.frequency)
        assertEquals("content://tree/backups", failing.folderUri)
        assertEquals("Backups", failing.folderName)
        assertEquals(AutoBackupError.FOLDER_UNAVAILABLE, failing.lastError)
        assertEquals(1_795_000_000_000L, failing.warningSnoozedUntil)
        assertTrue(failing.isFailureNotified)

        // A successful automatic backup clears the failure state.
        autoBackup.recordSuccess(1_795_100_000_000, isAuto = true)
        val fixed = autoBackup.getSettings().first()
        assertEquals(1_795_100_000_000L, fixed.lastBackupAt)
        assertEquals(1_795_100_000_000L, fixed.lastAutoBackupAt)
        assertNull(fixed.lastError)
        assertNull(fixed.warningSnoozedUntil)
        assertFalse(fixed.isFailureNotified)

        // A new folder without a name removes the old name.
        autoBackup.setFolder("content://tree/other", null)
        assertNull(autoBackup.getSettings().first().folderName)
    }

    @Test
    fun feedbackCountersAndBanners() = runTest {
        val feedback = FeedbackDataStore(testDataStore())

        assertFalse(feedback.isGettingStartedDismissed().first())
        assertFalse(feedback.isFirstSaveCelebrationPending().first())
        assertEquals(0, feedback.getTransactionCreatedCount().first())
        assertEquals(0, feedback.getReviewRequestCount().first())
        assertEquals(0L, feedback.getReviewRequestedAt().first())
        assertFalse(feedback.isFeedbackDialogShown().first())
        assertEquals(0L, feedback.getShareBannerHiddenUntil().first())
        assertEquals(0L, feedback.getNotificationCentreOpenedAt().first())
        assertEquals(0L, feedback.getReminderBannerHiddenUntil().first())
        assertEquals(0, feedback.getReminderBannerDismissCount().first())
        assertEquals(0, feedback.getNotificationPermissionAsks().first())
        assertNull(feedback.getLastSeenWhatsNew().first())
        assertNull(feedback.getLastActiveDay().first())
        assertEquals(emptySet<String>(), feedback.getSentBudgetAlerts().first())

        feedback.setGettingStartedDismissed()
        feedback.setFirstSaveCelebrationPending(true)
        repeat(3) { feedback.increaseTransactionCreatedCount() }
        feedback.setFeedbackDialogShown(true)
        feedback.setFeedbackDialogShown(true)
        feedback.hideShareBanner(1_795_000_000_000)
        feedback.setNotificationCentreOpenedAt(1_795_100_000_000)
        feedback.dismissReminderBanner(1_795_200_000_000)
        feedback.dismissReminderBanner(1_795_300_000_000)
        feedback.recordNotificationPermissionAsk()
        feedback.setLastSeenWhatsNew("1.4.34")
        feedback.addSentBudgetAlert("budget-1:85")
        feedback.addSentBudgetAlert("budget-1:100")

        assertTrue(feedback.isGettingStartedDismissed().first())
        assertTrue(feedback.isFirstSaveCelebrationPending().first())
        assertEquals(3, feedback.getTransactionCreatedCount().first())
        assertEquals(2, feedback.getReviewRequestCount().first())
        assertTrue(feedback.getReviewRequestedAt().first() > 0)
        assertTrue(feedback.isFeedbackDialogShown().first())
        assertEquals(1_795_000_000_000L, feedback.getShareBannerHiddenUntil().first())
        assertEquals(1_795_100_000_000L, feedback.getNotificationCentreOpenedAt().first())
        assertEquals(1_795_300_000_000L, feedback.getReminderBannerHiddenUntil().first())
        assertEquals(2, feedback.getReminderBannerDismissCount().first())
        assertEquals(1, feedback.getNotificationPermissionAsks().first())
        assertEquals("1.4.34", feedback.getLastSeenWhatsNew().first())
        assertEquals(setOf("budget-1:85", "budget-1:100"), feedback.getSentBudgetAlerts().first())
    }

    @Test
    fun feedbackHistoriesKeepTheirLimits() = runTest {
        val feedback = FeedbackDataStore(testDataStore())

        // Save times: invalid minutes are dropped, the newest 20 kept, oldest first.
        feedback.recordSaveTime(-5)
        feedback.recordSaveTime(24 * 60)
        (1..25).forEach { feedback.recordSaveTime(it * 10) }
        assertEquals((6..25).map { it * 10 }, feedback.getRecentSaveMinutes().first())

        // No-spend days also move the last active day.
        feedback.addNoSpendDay(20_400)
        assertEquals(20_400L, feedback.getLastActiveDay().first())
        feedback.setLastActiveDay(20_410)
        assertEquals(20_410L, feedback.getLastActiveDay().first())

        // Insights: newest first, at most MAX_INSIGHTS.
        (1..MAX_INSIGHTS + 5).forEach { feedback.addInsight("weekly_summary", "title $it", "message $it", it.toLong()) }
        val insights = feedback.getInsights().first()
        assertEquals(MAX_INSIGHTS, insights.size)
        assertEquals(StoredInsight("weekly_summary", "title ${MAX_INSIGHTS + 5}", "message ${MAX_INSIGHTS + 5}", (MAX_INSIGHTS + 5).toLong()), insights.first())
        assertEquals("title 6", insights.last().title)
    }
}
