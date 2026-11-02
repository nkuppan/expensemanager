package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.ExchangeRateSource
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

@Stable
data class TransactionCreateState(
    val amount: TextFieldValue<String>,
    val notes: TextFieldValue<String>,
    val dateTime: Date,
    val transactionType: TransactionType,
    val currency: Currency,
    val selectedCategory: Category,
    val selectedFromAccount: AccountUiModel,
    val selectedToAccount: AccountUiModel,
    val accounts: List<AccountUiModel>,
    val categories: List<Category>,
    val accountSelection: AccountSelection,
    val showDeleteDialog: Boolean,
    val showDeleteButton: Boolean,
    val showNumberPad: Boolean,
    // Current amount as a number, pre-filled into the keypad when it opens (null = empty).
    val numberPadInitialAmount: Double? = null,
    val showCategorySelection: Boolean,
    val showAccountSelection: Boolean,
    val showDateSelection: Boolean,
    val showTimeSelection: Boolean,
    val attachments: List<String> = emptyList(),
    val showAttachmentPicker: Boolean = false,
    /** Repeat rule for a new transaction; null = doesn't repeat. */
    val repeat: RecurringFrequency? = null,
    /** Only new transactions can be made recurring; editing one never changes a rule. */
    val canRepeat: Boolean = true,
    /** Set when editing a transaction a recurring rule logged and that rule still exists. */
    val recurringRuleId: String? = null,
    /** "Discard changes?" confirmation, shown when leaving a form with unsaved edits. */
    val showDiscardDialog: Boolean = false,
    /** Present only for a transfer between accounts in different currencies. */
    val exchangeRate: ExchangeRateInputState? = null,
    /** Why the last save was rejected, shown on screen (validation is strict). */
    val saveError: String? = null,
)

/**
 * The rate for a cross-currency transfer: 1 [baseCode] = [rateText] [quoteCode]. Copied from the
 * provider's fetch (only for transfers dated today), from the user's fixed rate for the pair,
 * or typed by the user; all are saved as the same snapshot shape, with [source] recording which.
 */
data class ExchangeRateInputState(
    val baseCode: String,
    val quoteCode: String,
    val rateText: String,
    val source: ExchangeRateSource,
    /** Provider + fetch time of the fetched rate, when one was fetched. */
    val provider: String? = null,
    val fetchedAt: Long? = null,
    val fetchedRate: Double? = null,
    /** Where [fetchedRate] came from (fetched, or the user's fixed rate); null when none. */
    val suggestedSource: ExchangeRateSource? = null,
    /** The transaction's existing snapshot, kept as-is while nothing it depends on changed. */
    val existingSnapshot: ExchangeRateSnapshot? = null,
    /** Why no rate was pre-filled (backdated / unavailable), or null. */
    val hint: String? = null,
    /** "≈ 13,787.5 SYP": what the destination account will receive. */
    val receivedAmount: String? = null,
    val isLoading: Boolean = false,
    val rateError: Boolean = false,
)

enum class AccountSelection {
    FROM_ACCOUNT,
    TO_ACCOUNT,
}

data class TransactionCreateInitSetupState(
    val isCategorySyncCompleted: Boolean,
    val isAccountSyncCompleted: Boolean,
)
