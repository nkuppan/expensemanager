package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionCreateViewModelTest : BaseCoroutineTest() {

    private val getCurrencyUseCase: GetCurrencyUseCase = mock()
    private val getAllAccountsUseCase: GetAllAccountsUseCase = mock()
    private val getAllCategoryUseCase: GetAllCategoryUseCase = mock()
    private val getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase = mock()
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase = mock()
    private val findTransactionByIdUseCase: FindTransactionByIdUseCase = mock()
    private val addTransactionUseCase: AddTransactionUseCase = mock()
    private val updateTransactionUseCase: UpdateTransactionUseCase = mock()
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()
    private val numberFormatRepository: NumberFormatRepository = mock()

    private lateinit var viewModel: TransactionCreateViewModel

    private val fakeCurrency = Currency(symbol = "$", name = "USD")

    private val fakeFromAccount = AccountUiModel(
        id = "from-account-id",
        name = "Checking",
        storedIcon = StoredIcon(name = "ic_wallet", backgroundColor = "#000000"),
        amount = Amount(1000.0),
        amountTextColor = 0,
    )

    private val fakeToAccount = AccountUiModel(
        id = "to-account-id",
        name = "Savings",
        storedIcon = StoredIcon(name = "ic_wallet", backgroundColor = "#000000"),
        amount = Amount(500.0),
        amountTextColor = 0,
    )

    private val fakeCategory = Category(
        id = "category-id",
        name = "Food",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "ic_food", backgroundColor = "#FF0000"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    override fun onCreate() {
        super.onCreate()

        whenever(getDefaultCurrencyUseCase.invoke()).thenReturn(fakeCurrency)
        whenever(numberFormatRepository.formatForEditing(0.0)).thenReturn("0.00")
        whenever(getCurrencyUseCase.invoke()).thenReturn(flowOf(fakeCurrency))
        whenever(getAllAccountsUseCase.invoke()).thenReturn(flowOf(emptyList()))
        whenever(getAllCategoryUseCase.invoke()).thenReturn(flowOf(emptyList()))
        whenever(settingsRepository.getDefaultAccount()).thenReturn(flowOf(null))
        whenever(settingsRepository.getDefaultIncomeCategory()).thenReturn(flowOf(null))
        whenever(settingsRepository.getDefaultExpenseCategory()).thenReturn(flowOf(null))

        viewModel = TransactionCreateViewModel(
            savedStateHandle = SavedStateHandle(),
            getCurrencyUseCase = getCurrencyUseCase,
            getAllAccountsUseCase = getAllAccountsUseCase,
            getAllCategoryUseCase = getAllCategoryUseCase,
            getDefaultCurrencyUseCase = getDefaultCurrencyUseCase,
            getFormattedAmountUseCase = getFormattedAmountUseCase,
            findTransactionByIdUseCase = findTransactionByIdUseCase,
            addTransactionUseCase = addTransactionUseCase,
            updateTransactionUseCase = updateTransactionUseCase,
            deleteTransactionUseCase = deleteTransactionUseCase,
            settingsRepository = settingsRepository,
            appComposeNavigator = appComposeNavigator,
            numberFormatRepository = numberFormatRepository,
        )
    }

    // region helpers

    private fun buildState(
        notes: String = "Test notes",
        dateTime: Date = Date(),
        transactionType: TransactionType = TransactionType.EXPENSE,
        selectedCategory: Category = fakeCategory,
        selectedFromAccount: AccountUiModel = fakeFromAccount,
        selectedToAccount: AccountUiModel = fakeToAccount,
    ) = TransactionCreateState(
        amount = TextFieldValue(value = "100.00", valueError = false, onValueChange = null),
        notes = TextFieldValue(value = notes, valueError = false, onValueChange = null),
        dateTime = dateTime,
        transactionType = transactionType,
        currency = fakeCurrency,
        selectedCategory = selectedCategory,
        selectedFromAccount = selectedFromAccount,
        selectedToAccount = selectedToAccount,
        accounts = emptyList(),
        categories = emptyList(),
        accountSelection = AccountSelection.FROM_ACCOUNT,
        showDeleteDialog = false,
        showDeleteButton = false,
        showNumberPad = false,
        showCategorySelection = false,
        showAccountSelection = false,
        showDateSelection = false,
        showTimeSelection = false,
    )

    private fun setEditingTransaction(transaction: Transaction) {
        val field = TransactionCreateViewModel::class.java.getDeclaredField("editingTransaction")
        field.isAccessible = true
        field.set(viewModel, transaction)
    }

    // endregion

    // region toAccountId

    @Test
    fun `expense transaction sets toAccountId to null`() {
        val result = viewModel.buildTransactionFromState(
            buildState(transactionType = TransactionType.EXPENSE),
            amountValue = 100.0,
        )

        assertThat(result.toAccountId).isNull()
    }

    @Test
    fun `income transaction sets toAccountId to null`() {
        val result = viewModel.buildTransactionFromState(
            buildState(transactionType = TransactionType.INCOME),
            amountValue = 100.0,
        )

        assertThat(result.toAccountId).isNull()
    }

    @Test
    fun `transfer transaction sets toAccountId to the to-account id`() {
        val result = viewModel.buildTransactionFromState(
            buildState(
                transactionType = TransactionType.TRANSFER,
                selectedToAccount = fakeToAccount,
            ),
            amountValue = 100.0,
        )

        assertThat(result.toAccountId).isEqualTo(fakeToAccount.id)
    }

    // endregion

    // region id (create vs edit mode)

    @Test
    fun `create mode generates a non-blank id`() {
        val result = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)

        assertThat(result.id).isNotEmpty()
    }

    @Test
    fun `create mode generates a unique id on each call`() {
        val first = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)
        val second = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)

        assertThat(first.id).isNotEqualTo(second.id)
    }

    @Test
    fun `edit mode reuses the existing transaction id`() {
        setEditingTransaction(FAKE_EXPENSE_TRANSACTION.copy(id = "existing-id-123"))

        val result = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)

        assertThat(result.id).isEqualTo("existing-id-123")
    }

    // endregion

    // region field mapping

    @Test
    fun `maps notes from state`() {
        val result = viewModel.buildTransactionFromState(
            buildState(notes = "Dinner at restaurant"),
            amountValue = 100.0,
        )

        assertThat(result.notes).isEqualTo("Dinner at restaurant")
    }

    @Test
    fun `maps categoryId from the selected category`() {
        val result = viewModel.buildTransactionFromState(
            buildState(selectedCategory = fakeCategory),
            amountValue = 100.0,
        )

        assertThat(result.categoryId).isEqualTo(fakeCategory.id)
    }

    @Test
    fun `maps fromAccountId from the selected from-account`() {
        val result = viewModel.buildTransactionFromState(
            buildState(selectedFromAccount = fakeFromAccount),
            amountValue = 100.0,
        )

        assertThat(result.fromAccountId).isEqualTo(fakeFromAccount.id)
    }

    @Test
    fun `maps transaction type from state`() {
        TransactionType.entries.forEach { type ->
            val result = viewModel.buildTransactionFromState(
                buildState(transactionType = type),
                amountValue = 100.0,
            )

            assertThat(result.type).isEqualTo(type)
        }
    }

    @Test
    fun `maps amount from the amountValue parameter`() {
        val result = viewModel.buildTransactionFromState(buildState(), amountValue = 250.75)

        assertThat(result.amount.amount).isEqualTo(250.75)
    }

    @Test
    fun `maps state dateTime as createdOn`() {
        val fixedDate = Date(1_000_000_000L)

        val result = viewModel.buildTransactionFromState(
            buildState(dateTime = fixedDate),
            amountValue = 100.0,
        )

        assertThat(result.createdOn).isEqualTo(fixedDate)
    }

    @Test
    fun `sets updatedOn to the current time`() {
        val before = Date()
        val result = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)
        val after = Date()

        assertThat(result.updatedOn.time).isAtLeast(before.time)
        assertThat(result.updatedOn.time).isAtMost(after.time)
    }

    @Test
    fun `sets imagePath to empty string`() {
        val result = viewModel.buildTransactionFromState(buildState(), amountValue = 100.0)

        assertThat(result.imagePath).isEmpty()
    }

    // endregion
}
