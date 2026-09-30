package com.naveenapps.expensemanager.feature.transaction.import.review

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.ParsedTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

@Stable
data class ImportDraft(
    val parsed: ParsedTransaction,
    val amountText: String,
    val notes: String,
    val dateTime: Date,
    val transactionType: TransactionType,
    val selectedAccount: AccountUiModel?,
    val selectedCategory: Category?,
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true,
    val showAccountSelection: Boolean = false,
    val showCategorySelection: Boolean = false,
    val showDateSelection: Boolean = false,
    val showTimeSelection: Boolean = false,
    val dateManuallyCorrected: Boolean = false,
) {
    // A draft is importable only when the bank reports SUCCESS and the date
    // parsed cleanly (or the user has manually corrected the date). Failed
    // bank transactions and unparseable rows can be reviewed but never saved.
    // Duplicate detection is advisory (warning shown on card) per issue #30.
    val isImportable: Boolean
        get() = parsed.isSuccess &&
            (parsed.parseError == null || dateManuallyCorrected)
}

enum class ImportViewMode {
    CARD,
    LIST,
}

enum class ImportReviewAction {
    ADDED,
    SKIPPED,
}

@Stable
data class ImportState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val drafts: List<ImportDraft> = emptyList(),
    val currentIndex: Int = 0,
    val viewMode: ImportViewMode = ImportViewMode.CARD,
    val accounts: List<AccountUiModel> = emptyList(),
    val categories: List<Category> = emptyList(),
    val acceptedCount: Int = 0,
    val rejectedCount: Int = 0,
    val lastAction: ImportReviewAction? = null,
    val canUndo: Boolean = false,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val duplicateCheckAvailable: Boolean = true,
    val savedCount: Int = 0,
    val saveTotal: Int = 0,
    val topExpenseCategories: List<Category> = emptyList(),
    val topIncomeCategories: List<Category> = emptyList(),
    /** Password remembered from an earlier import, null when none is stored. */
    val rememberedPassword: String? = null,
    /** "Remember password" checkbox in the password dialog. */
    val rememberPassword: Boolean = false,
    /** Set once [rememberedPassword] has been read, so the dialog can prefill. */
    val isRememberedPasswordLoaded: Boolean = false,
) {
    val totalCount: Int get() = drafts.size
    val readyCount: Int get() = drafts.count { it.isImportable }
    val unavailableCount: Int get() = totalCount - readyCount
    val duplicateCount: Int get() = drafts.count { it.isDuplicate && it.isImportable }
    val validSelectedCount: Int get() = drafts.count { it.isSelected && it.isImportable }
    val canConfirmSelection: Boolean get() = validSelectedCount > 0 && !isSaving
    val reviewedCount: Int get() = acceptedCount + rejectedCount
    val reviewProgress: Float
        get() = if (totalCount == 0) 0f else (reviewedCount.toFloat() / totalCount).coerceIn(0f, 1f)
    val pendingDrafts: List<ImportDraft> get() = if (currentIndex in drafts.indices) drafts.drop(currentIndex) else emptyList()
    val currentDraft: ImportDraft? get() = drafts.getOrNull(currentIndex)
}
