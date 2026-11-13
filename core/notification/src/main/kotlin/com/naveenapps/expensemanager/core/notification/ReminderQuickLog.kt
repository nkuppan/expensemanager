package com.naveenapps.expensemanager.core.notification

import android.content.Context
import android.content.Intent
import com.naveenapps.expensemanager.core.domain.usecase.account.FindAccountByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.FindCategoryByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetFrequentTransactionsUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetAlertTrigger
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import java.time.LocalTime
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.first

/**
 * The user's most repeated expense, offered as a button on the daily reminder ("Tea ₹20"), so
 * the most common entry is logged without opening the app. Carried in the button's intent, so
 * the entry saved is exactly the one shown, even if the user's history changed since.
 */
data class ReminderQuickPick(
    val categoryId: String,
    val accountId: String,
    val amount: Double,
    val notes: String,
    /** Button text: the note (or category) and the formatted amount, e.g. "Tea ₹20". */
    val label: String,
) {
    fun putInto(intent: Intent): Intent = intent
        .putExtra(EXTRA_CATEGORY_ID, categoryId)
        .putExtra(EXTRA_ACCOUNT_ID, accountId)
        .putExtra(EXTRA_AMOUNT, amount)
        .putExtra(EXTRA_NOTES, notes)
        .putExtra(EXTRA_LABEL, label)

    companion object {
        private const val EXTRA_CATEGORY_ID = "quick_pick_category_id"
        private const val EXTRA_ACCOUNT_ID = "quick_pick_account_id"
        private const val EXTRA_AMOUNT = "quick_pick_amount"
        private const val EXTRA_NOTES = "quick_pick_notes"
        private const val EXTRA_LABEL = "quick_pick_label"

        fun from(intent: Intent): ReminderQuickPick? {
            val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: return null
            val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID) ?: return null
            val amount = intent.getDoubleExtra(EXTRA_AMOUNT, -1.0).takeIf { it >= 0.0 } ?: return null
            return ReminderQuickPick(
                categoryId = categoryId,
                accountId = accountId,
                amount = amount,
                notes = intent.getStringExtra(EXTRA_NOTES).orEmpty(),
                label = intent.getStringExtra(EXTRA_LABEL).orEmpty(),
            )
        }
    }
}

/**
 * Finds the reminder's quick pick, and saves / undoes it from the notification's buttons. A
 * save does what the add screen does for a new expense (count, "logged today", save time for
 * the reminder suggestion, budget alert check), with `source = reminder` in analytics.
 */
class ReminderQuickLog(
    private val context: Context,
    private val getFrequentTransactionsUseCase: GetFrequentTransactionsUseCase,
    private val findCategoryByIdUseCase: FindCategoryByIdUseCase,
    private val findAccountByIdUseCase: FindAccountByIdUseCase,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val feedbackRepository: FeedbackRepository,
    private val budgetAlertTrigger: BudgetAlertTrigger,
    private val analyticsRepository: AnalyticsRepository,
) {

    /** The most used repeated expense (2+ times in 90 days), or null when there's none yet. */
    suspend fun topPick(): ReminderQuickPick? {
        val entry = getFrequentTransactionsUseCase.invoke().first()
            .firstOrNull { it.type == TransactionType.EXPENSE } ?: return null
        val category = (findCategoryByIdUseCase(entry.categoryId) as? Resource.Success)?.data ?: return null
        if (findAccountByIdUseCase(entry.fromAccountId) !is Resource.Success) return null

        val name = entry.notes.ifBlank {
            category.titleResId?.let { context.getString(it) } ?: category.name
        }
        val currency = getCurrencyUseCase.invoke().first()
        val amountText = getFormattedAmountUseCase.invoke(entry.amount, currency).amountString.orEmpty()
        return ReminderQuickPick(
            categoryId = entry.categoryId,
            accountId = entry.fromAccountId,
            amount = entry.amount,
            notes = entry.notes,
            label = "${name.ellipsize(MAX_NAME_LENGTH)} $amountText".trim(),
        )
    }

    /** Saves [pick] as today's expense; the new transaction's id, or null if it couldn't be. */
    suspend fun log(pick: ReminderQuickPick): String? {
        // The category or account may have been deleted since the reminder was posted.
        if (findCategoryByIdUseCase(pick.categoryId) !is Resource.Success) return null
        if (findAccountByIdUseCase(pick.accountId) !is Resource.Success) return null

        val now = Date()
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            notes = pick.notes,
            categoryId = pick.categoryId,
            fromAccountId = pick.accountId,
            toAccountId = null,
            type = TransactionType.EXPENSE,
            amount = Amount(pick.amount),
            imagePath = "",
            createdOn = now,
            updatedOn = now,
        )
        if (addTransactionUseCase.invoke(transaction) !is Resource.Success) return null

        feedbackRepository.setTransactionCreated(true)
        feedbackRepository.recordSaveTime(LocalTime.now().let { it.hour * 60 + it.minute })
        budgetAlertTrigger.checkBudgetsSoon()

        val count = feedbackRepository.getTransactionCreatedCount().first()
        analyticsRepository.logEvent(
            AnalyticsEvents.TRANSACTION_CREATED,
            mapOf(
                AnalyticsParams.TYPE to TransactionType.EXPENSE.name.lowercase(),
                AnalyticsParams.IS_FIRST to (count <= 1).toString(),
                AnalyticsParams.TRANSACTION_COUNT to count.toString(),
                AnalyticsParams.HAS_NOTES to pick.notes.isNotBlank().toString(),
                AnalyticsParams.HAS_ATTACHMENT to false.toString(),
                AnalyticsParams.SOURCE to SOURCE_REMINDER,
            ),
        )
        analyticsRepository.logEvent(AnalyticsEvents.REMINDER_QUICK_LOGGED, emptyMap())
        return transaction.id
    }

    /** Removes a quick-logged expense again ("Undo"); true when it was deleted. */
    suspend fun undo(transactionId: String): Boolean {
        val transaction = (transactionRepository.findTransactionById(transactionId) as? Resource.Success)?.data
            ?: return false
        val deleted = deleteTransactionUseCase.invoke(transaction) is Resource.Success
        if (deleted) {
            budgetAlertTrigger.checkBudgetsSoon()
            analyticsRepository.logEvent(AnalyticsEvents.REMINDER_QUICK_LOG_UNDONE, emptyMap())
        }
        return deleted
    }

    private fun String.ellipsize(max: Int): String = if (length <= max) this else take(max - 1).trimEnd() + "…"

    private companion object {
        const val SOURCE_REMINDER = "reminder"
        const val MAX_NAME_LENGTH = 16
    }
}
