package com.naveenapps.expensemanager.feature.transaction.recurring.edit

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.transaction.create.AccountSelection
import java.util.Date

data class RecurringTransactionEditState(
    /** False until the rule and the account/category lists have loaded. */
    val isLoaded: Boolean = false,
    val amount: TextFieldValue<String>,
    val notes: TextFieldValue<String>,
    /** Fixed once a rule exists: changing it would change which accounts are required. */
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDate: Date = Date(),
    /** True when frequency or next date differ from the saved rule (saving starts a new series). */
    val isScheduleChanged: Boolean = false,
    val categories: List<Category> = emptyList(),
    val accounts: List<AccountUiModel> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedFromAccount: AccountUiModel? = null,
    val selectedToAccount: AccountUiModel? = null,
    val accountSelection: AccountSelection = AccountSelection.FROM_ACCOUNT,
    val showNumberPad: Boolean = false,
    val numberPadInitialAmount: Double? = null,
    val showCategorySelection: Boolean = false,
    val showAccountSelection: Boolean = false,
    val showDateSelection: Boolean = false,
    val showTimeSelection: Boolean = false,
    val showStopDialog: Boolean = false,
    val showDiscardDialog: Boolean = false,
)
