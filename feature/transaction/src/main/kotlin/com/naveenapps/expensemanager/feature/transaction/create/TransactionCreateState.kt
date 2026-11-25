package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.time.Instant

@Stable
data class TransactionCreateState(
    val amount: TextFieldValue<String>,
    val notes: TextFieldValue<String>,
    val dateTime: Instant,
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
    /** Only new transactions can be made recurring; editing one never changes a rule. */
    val canRepeat: Boolean = true,
    /** Set when editing a transaction a recurring rule logged and that rule still exists. */
    val recurringRuleId: String? = null,
    /** When transactionType is REFUND, the id of the original transaction being refunded. */
    val originalTransactionId: String? = null,
    /** Whether the original transaction selection bottom sheet is shown. */
    val showOriginalTransactionSelection: Boolean = false,
    /** Prior expense/income entries on the refund's account to link as the original. */
    val refundCandidates: List<Transaction> = emptyList(),
    /** The original transaction a refund is linked to, for display. */
    val originalTransaction: Transaction? = null,
    /** Repeat rule for a new transaction; null = doesn't repeat. */
    val repeat: RecurringFrequency? = null,
    /** "Discard changes?" confirmation, shown when leaving a form with unsaved edits. */
    val showDiscardDialog: Boolean = false,
    /** Frequent entries offered as one-tap chips; only for a new transaction. */
    val quickPicks: List<QuickPick> = emptyList(),
    /** Only a new transaction can be filled from a scanned receipt. */
    val canScanReceipt: Boolean = true,
    /** A shared or scanned receipt is being read (on-device text recognition). */
    val isReadingReceipt: Boolean = false,
    /** Categories used before for the receipt's payee, most used first; shown as chips. */
    val suggestedCategories: List<Category> = emptyList(),
)

enum class AccountSelection {
    FROM_ACCOUNT,
    TO_ACCOUNT,
}

data class TransactionCreateInitSetupState(
    val isCategorySyncCompleted: Boolean,
    val isAccountSyncCompleted: Boolean,
)
