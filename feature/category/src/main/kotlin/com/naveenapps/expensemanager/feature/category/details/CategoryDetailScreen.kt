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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardViewDefaults
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.components.TransactionItem
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
import java.util.Date
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDetailsContent(
    state: CategoryDetailsState,
    onAction: (CategoryDetailsAction) -> Unit,
) {
    val categoryTransaction = state.categoryTransaction
    val categoryName = categoryTransaction?.category?.let { category ->
        category.titleResId?.let { stringResource(it) } ?: category.name
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onAction.invoke(CategoryDetailsAction.ClosePage) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(DesignSystemR.string.a11y_close),
                        )
                    }
                },
                title = {
                    Text(
                        text = categoryName.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (categoryTransaction != null) {
                        IconButton(onClick = { onAction.invoke(CategoryDetailsAction.OpenCategoryEdit) }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_category),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onAction.invoke(CategoryDetailsAction.OpenTransactionCreate) },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_transaction),
                )
            }
        },
    ) { innerPadding ->
        val modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        if (categoryTransaction == null || categoryName == null) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        ) {
            item(key = "summary") {
                CategorySummaryCard(
                    categoryTransaction = categoryTransaction,
                    name = categoryName,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            item(key = "transactions_header") {
                SectionHeader(text = stringResource(R.string.category_transactions))
            }

            if (state.transactions.isEmpty()) {
                item(key = "empty") {
                    EmptyItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        emptyItemText = stringResource(id = R.string.no_transactions_available),
                        icon = DesignSystemR.drawable.ic_no_transaction,
                    )
                }
            } else {
                itemsIndexed(
                    items = state.transactions,
                    key = { _, item -> "transaction_${item.id}" },
                ) { index, item ->
                    AppCardView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 2.dp),
                        shape = AppCardViewDefaults.cardShape(index, state.transactions),
                    ) {
                        TransactionItem(
                            modifier = Modifier.fillMaxWidth(),
                            categoryName = item.categoryTitleResId?.let { stringResource(it) } ?: item.categoryName,
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

    AppCardView(
        modifier = modifier.fillMaxWidth(),
        cornerSize = 24.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconAndBackgroundView(
                    icon = category.storedIcon.name,
                    iconBackgroundColor = category.storedIcon.backgroundColor,
                    name = name,
                    customImagePath = category.storedIcon.customImagePath,
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            id = if (category.type == CategoryType.INCOME) R.string.income else R.string.expense,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = categoryTransaction.amount.amountString.orEmpty(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(share)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.category_share_of_total, shareText),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                )
            }
        }
    }
}

/** Small caps section title, as on budget details and the Categories tab. */
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
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
    date = Date().toCompleteDateWithDate(),
)

@AppPreviewsLightAndDarkMode
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

@AppPreviewsLightAndDarkMode
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
