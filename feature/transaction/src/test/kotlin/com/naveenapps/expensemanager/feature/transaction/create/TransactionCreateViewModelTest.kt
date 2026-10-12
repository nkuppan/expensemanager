package com.naveenapps.expensemanager.feature.transaction.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.CreateRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
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
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetAlertTrigger
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.core.testing.FAKE_EXPENSE_TRANSACTION
import com.naveenapps.expensemanager.feature.transaction.R
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
    private val imageStorageRepository: ImageStorageRepository = mock()
    private val appComposeNavigator: AppComposeNavigator = mock()
    private val numberFormatRepository: NumberFormatRepository = mock()
    private val feedbackRepository: FeedbackRepository = mock()
    private val analyticsRepository: AnalyticsRepository = mock()
    private val budgetAlertTrigger: BudgetAlertTrigger = mock()
    private val createRecurringTransactionUseCase: CreateRecurringTransactionUseCase = mock()

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
        whenever(feedbackRepository.getTransactionCreatedCount()).thenReturn(flowOf(1))
        whenever(feedbackRepository.shouldShowFeedbackDialog()).thenReturn(flowOf(false))
        whenever(getFormattedAmountUseCase.invoke(any(), any())).thenReturn(Amount(0.0))

        viewModel = createViewModel()
    }

    private fun createViewModel() = TransactionCreateViewModel(
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
        imageStorageRepository = imageStorageRepository,
        appComposeNavigator = appComposeNavigator,
        numberFormatRepository = numberFormatRepository,
        feedbackRepository = feedbackRepository,
        analyticsRepository = analyticsRepository,
        budgetAlertTrigger = budgetAlertTrigger,
        createRecurringTransactionUseCase = createRecurringTransactionUseCase,
    )

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

    // region number pad

    @Test
    fun `new transaction opens straight on the number pad`() {
        assertThat(viewModel.state.value.showNumberPad).isTrue()
    }

    @Test
    fun `dismissing the number pad without a value closes it`() {
        viewModel.processAction(TransactionCreateAction.SetNumberPadValue(null))

        assertThat(viewModel.state.value.showNumberPad).isFalse()
    }

    // endregion

    // region account / category selection

    private fun account(id: String) = Account(
        id = id,
        name = id,
        type = AccountType.REGULAR,
        storedIcon = StoredIcon(name = "ic_wallet", backgroundColor = "#000000"),
        createdOn = Date(),
        updatedOn = Date(),
    )

    private fun category(id: String) = fakeCategory.copy(id = id, name = id)

    @Test
    fun `transfer defaults the to-account to a different account`() = runTest {
        whenever(getAllAccountsUseCase.invoke()).thenReturn(flowOf(listOf(account("a"), account("b"))))
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionCreateAction.ChangeTransactionType(TransactionType.TRANSFER))

        assertThat(viewModel.state.value.selectedFromAccount.id).isEqualTo("a")
        assertThat(viewModel.state.value.selectedToAccount.id).isEqualTo("b")
    }

    @Test
    fun `account list update keeps the user's selection`() = runTest {
        val accounts = MutableStateFlow(listOf(account("a"), account("b")))
        whenever(getAllAccountsUseCase.invoke()).thenReturn(accounts)
        viewModel = createViewModel()
        advanceUntilIdle()

        val chosen = viewModel.state.value.accounts.first { it.id == "b" }
        viewModel.processAction(TransactionCreateAction.ShowAccountSelection(AccountSelection.FROM_ACCOUNT))
        viewModel.processAction(TransactionCreateAction.SelectAccount(chosen))
        // e.g. a balance changed somewhere — same ids, new emission
        accounts.value = listOf(account("a"), account("b").copy(amount = 10.0))
        advanceUntilIdle()

        assertThat(viewModel.state.value.selectedFromAccount.id).isEqualTo("b")
    }

    @Test
    fun `newly created account is selected for the side being picked`() = runTest {
        val accounts = MutableStateFlow(listOf(account("a"), account("b")))
        whenever(getAllAccountsUseCase.invoke()).thenReturn(accounts)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionCreateAction.ShowAccountSelection(AccountSelection.TO_ACCOUNT))
        accounts.value = listOf(account("a"), account("b"), account("new"))
        advanceUntilIdle()

        assertThat(viewModel.state.value.selectedToAccount.id).isEqualTo("new")
        assertThat(viewModel.state.value.selectedFromAccount.id).isEqualTo("a")
    }

    @Test
    fun `category list update keeps the selection and selects a newly created category`() = runTest {
        val categories = MutableStateFlow(listOf(category("c1"), category("c2")))
        whenever(getAllCategoryUseCase.invoke()).thenReturn(categories)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processAction(TransactionCreateAction.SelectCategory(category("c2")))
        categories.value = listOf(category("c1"), category("c2").copy(name = "renamed"))
        advanceUntilIdle()
        assertThat(viewModel.state.value.selectedCategory.id).isEqualTo("c2")

        categories.value = categories.value + category("c3")
        advanceUntilIdle()
        assertThat(viewModel.state.value.selectedCategory.id).isEqualTo("c3")
    }

    @Test
    fun `saving a transfer between the same account shows an error`() = runTest {
        whenever(getAllAccountsUseCase.invoke()).thenReturn(flowOf(listOf(account("only"))))
        whenever(numberFormatRepository.parseToDouble("50")).thenReturn(50.0)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(TransactionCreateAction.SetNumberPadValue("50"))
            viewModel.processAction(TransactionCreateAction.ChangeTransactionType(TransactionType.TRANSFER))
            viewModel.processAction(TransactionCreateAction.Save)

            assertThat(awaitItem()).isEqualTo(
                TransactionCreateEvent.ShowMessage(R.string.select_different_account),
            )
        }
    }

    // endregion
}
