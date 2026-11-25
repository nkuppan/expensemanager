package com.naveenapps.expensemanager.feature.category.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountSize
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmEmptyState
import com.naveenapps.expensemanager.core.designsystem.em.EmFab
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmLoading
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSectionHeader
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
import com.naveenapps.expensemanager.core.designsystem.ui.utils.categoryDisplayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getColorValue
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryTransaction
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.feature.category.R
import com.naveenapps.expensemanager.feature.filter.type.getCategory
import java.text.NumberFormat
import kotlin.time.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoryDetailScreen(
    viewModel: CategoryDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CategoryDetailsContent(state, viewModel::processAction)
}

/**
 * A category's spending for the selected period, laid out like budget details: a plain top bar
 * with the category's name, a summary card (icon, total, and its share of all spending or income),
 * then its transactions in the app's standard rows.
 */
@Composable
private fun CategoryDetailsContent(
    state: CategoryDetailsState,
    onAction: (CategoryDetailsAction) -> Unit,
) {
    val categoryTransaction = state.categoryTransaction
    val categoryName = categoryTransaction?.category?.let { category ->
        category.displayName()
    }

    EmScaffold(
        topBar = {
            EmTopBar(
                title = categoryName.orEmpty(),
                navigation = EmTopBarNavigation.Close,
                onNavigationClick = { onAction.invoke(CategoryDetailsAction.ClosePage) },
                actions = {
                    if (categoryTransaction != null) {
                        EmIconButton(
                            icon = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_category),
                            onClick = { onAction.invoke(CategoryDetailsAction.OpenCategoryEdit) },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            EmFab(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.add_transaction),
                onClick = { onAction.invoke(CategoryDetailsAction.OpenTransactionCreate) },
            )
        },
    ) { innerPadding ->
        val modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        if (categoryTransaction == null || categoryName == null) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                EmLoading()
            }
            return@EmScaffold
        }

        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(top = EmTheme.spacing.sm, bottom = 96.dp),
        ) {
            item(key = "summary") {
                CategorySummaryCard(
                    categoryTransaction = categoryTransaction,
                    name = categoryName,
                    modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
                )
            }

            item(key = "transactions_header") {
                SectionHeader(text = stringResource(R.string.category_transactions))
            }

            if (state.transactions.isEmpty()) {
                item(key = "empty") {
                    EmEmptyState(
                        illustration = painterResource(id = DesignSystemR.drawable.ic_no_transaction),
                        title = stringResource(id = R.string.no_transactions_available),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = EmTheme.spacing.xl),
                    )
                }
            } else {
                itemsIndexed(
                    items = state.transactions,
                    key = { _, item -> "transaction_${item.id}" },
                ) { index, item ->
                    EmCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = EmTheme.spacing.lg,
                                end = EmTheme.spacing.lg,
                                bottom = EmTheme.spacing.xxs,
                            ),
                        position = EmCardPosition.of(index, state.transactions),
                    ) {
                        TransactionItem(
                            modifier = Modifier.fillMaxWidth(),
                            categoryName = item.categoryDisplayName(),
                            categoryColor = item.categoryIcon.backgroundColor,
                            categoryIcon = item.categoryIcon.name,
                            amount = item.amount,
                            date = item.date,
                            notes = item.notes,
                            transactionType = item.transactionType,
                            isRecurring = item.isRecurring,
                            fromAccountName = item.fromAccountName,
                            fromAccountIcon = item.fromAccountIcon.name,
                            fromAccountColor = item.fromAccountIcon.backgroundColor,
                            toAccountName = item.toAccountName,
                            toAccountIcon = item.toAccountIcon?.name,
                            toAccountColor = item.toAccountIcon?.backgroundColor,
                            onClick = { onAction.invoke(CategoryDetailsAction.OpenTransactionEdit(item.id)) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * The category's icon and total for the period, then a bar and pill for its share of all spending
 * (or income), in the category's own colour. Same card shape as the budget gauge.
 */
@Composable
private fun CategorySummaryCard(
    categoryTransaction: CategoryTransaction,
    name: String,
    modifier: Modifier = Modifier,
) {
    val category = categoryTransaction.category
    val color = Color(getColorValue(category.storedIcon.backgroundColor))
    val share = (categoryTransaction.percent / 100f).takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    val shareText = remember(share) { NumberFormat.getPercentInstance().format(share.toDouble()) }

    EmCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.xl)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconAndBackgroundView(
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                    name = name,
                    customImagePath = category.storedIcon.customImagePath,
                )
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    EmText(
                        text = stringResource(
                            id = if (category.type == CategoryType.INCOME) R.string.income else R.string.expense,
                        ),
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                    EmAmountText(
                        amount = categoryTransaction.amount.amountString.orEmpty(),
                        size = EmAmountSize.Large,
                    )
                }
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.lg))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(EmTheme.shapes.pill)
                        .background(color.tint()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(share)
                            .height(8.dp)
                            .clip(EmTheme.shapes.pill)
                            .background(color),
                    )
                }
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                EmText(
                    text = stringResource(R.string.category_share_of_total, shareText),
                    modifier = Modifier
                        .clip(EmTheme.shapes.pill)
                        .background(color.tint())
                        .padding(horizontal = EmTheme.spacing.sm, vertical = EmTheme.spacing.xs),
                    role = EmTextRole.Label,
                    color = color,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Small caps section title, as on budget details and the Categories tab. */
@Composable
private fun SectionHeader(text: String) {
    EmSectionHeader(
        title = text,
        modifier = Modifier.padding(
            start = EmTheme.spacing.lg,
            end = EmTheme.spacing.lg,
            top = EmTheme.spacing.xl,
            bottom = EmTheme.spacing.xs,
        ),
    )
}

fun getTransactionItem(id: String) = TransactionUiItem(
    id = id,
    notes = "Sample Description",
    amount = Amount(amount = 300.0, amountString = "300.00 ₹"),
    categoryName = "Clothing",
    transactionType = TransactionType.EXPENSE,
    categoryIcon = StoredIcon(
        name = "agriculture",
        backgroundColor = "#000000",
    ),
    fromAccountName = "DB Bank xxxx",
    fromAccountIcon = StoredIcon(
        name = "account_balance",
        backgroundColor = "#000000",
    ),
    date = Clock.System.now().toCompleteDateWithDate(),
)

@EmPreviews
@Composable
fun CategoryDetailsPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryDetailsContent(
            state = CategoryDetailsState(
                categoryTransaction = CategoryTransaction(
                    category = getCategory(0),
                    amount = Amount(100.0, amountString = "$100.00"),
                    percent = 42.0f,
                    transaction = emptyList(),
                ),
                transactions = buildList {
                    repeat(5) { index ->
                        add(getTransactionItem(index.toString()))
                    }
                },
            ),
            onAction = {},
        )
    }
}

@EmPreviews
@Composable
fun CategoryDetailsEmptyPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        CategoryDetailsContent(
            state = CategoryDetailsState(
                categoryTransaction = CategoryTransaction(
                    category = getCategory(0),
                    amount = Amount(0.0, amountString = "$0.00"),
                    percent = 0f,
                    transaction = emptyList(),
                ),
                transactions = emptyList(),
            ),
            onAction = {},
        )
    }
}
