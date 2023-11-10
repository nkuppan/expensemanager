package com.naveenapps.expensemanager.presentation.account.list

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.domain.model.UiState
import com.naveenapps.expensemanager.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.ui.extensions.getDrawable
import com.naveenapps.expensemanager.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.ui.utils.ItemSpecModifier
import com.naveenapps.expensemanager.ui.utils.UiText


@Composable
fun AccountListScreen(
    navController: NavController
) {
    val viewModel: AccountListViewModel = hiltViewModel()
    val accountUiState by viewModel.accounts.collectAsState()
    AccountListScreenScaffoldView(navController, accountUiState)
}

@Composable
private fun AccountListScreenScaffoldView(
    navController: NavController,
    accountUiState: UiState<List<AccountUiModel>>
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopNavigationBar(
                navController = navController,
                title = stringResource(R.string.account)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("account/create")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        AccountListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            accountUiState = accountUiState
        ) { account ->
            navController.navigate("account/create?accountId=${account.id}")
        }
    }
}

@Composable
private fun AccountListScreenContent(
    accountUiState: UiState<List<AccountUiModel>>,
    modifier: Modifier = Modifier,
    onItemClick: ((AccountUiModel) -> Unit)? = null
) {

    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    Box(modifier = modifier) {

        when (accountUiState) {
            UiState.Empty -> {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .align(Alignment.Center),
                    text = stringResource(id = R.string.no_account_available),
                    textAlign = TextAlign.Center
                )
            }

            UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.Center)
                )
            }

            is UiState.Success -> {

                LazyColumn(state = scrollState) {
                    items(accountUiState.data) { account ->
                        AccountItem(
                            modifier = Modifier
                                .clickable {
                                    onItemClick?.invoke(account)
                                }
                                .then(ItemSpecModifier),
                            name = account.name,
                            icon = account.icon,
                            iconBackgroundColor = account.iconBackgroundColor,
                            amount = account.amount.asString(context)
                        )
                    }
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun AccountItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    amount: String?,
    modifier: Modifier = Modifier,
    @DrawableRes endIcon: Int? = null
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (amount != null) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = amount,
                style = MaterialTheme.typography.titleMedium,
                color = if (true)
                    colorResource(id = R.color.red_500)
                else
                    colorResource(id = R.color.green_500)
            )
        }
        if (endIcon != null) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 8.dp),
                painter = painterResource(id = endIcon),
                contentDescription = null,
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun AccountCheckedItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null
) {
    Row(modifier = modifier) {
        IconAndBackgroundView(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            icon = icon,
            iconBackgroundColor = iconBackgroundColor,
            name = name
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 16.dp)
                .align(Alignment.CenterVertically),
            text = name,
            style = MaterialTheme.typography.bodyLarge,
        )
        Checkbox(
            modifier = Modifier
                .align(Alignment.CenterVertically),
            checked = isSelected,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DashBoardAccountItem(
    name: String,
    icon: String,
    modifier: Modifier = Modifier,
    amount: UiText,
    amountTextColor: Color,
    backgroundColor: Color,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .width(160.dp)
        ) {
            Row {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = name,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    modifier = Modifier.padding(start = 8.dp),
                    painter = painterResource(id = context.getDrawable(icon)),
                    contentDescription = name,
                )
            }
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 16.dp),
                color = amountTextColor,
                text = amount.asString(context),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

val ACCOUNT_DUMMY_DATA = listOf(
    AccountUiModel(
        id = "1",
        name = "Cash",
        icon = "account_balance_wallet",
        iconBackgroundColor = "#000000",
        amountTextColor = R.color.green_500,
        amount = UiText.DynamicString("$100.00")
    ),
    AccountUiModel(
        id = "2",
        name = "Bank Account - xxxx",
        icon = "account_balance",
        iconBackgroundColor = "#000000",
        amountTextColor = R.color.green_500,
        amount = UiText.DynamicString("$100.00")
    ),
    AccountUiModel(
        id = "3",
        name = "Credit Card - xxxx",
        icon = "credit_card",
        iconBackgroundColor = "#000000",
        amountTextColor = R.color.green_500,
        amount = UiText.DynamicString("$100.00")
    ),
)

@Preview
@Composable
private fun DashBoardAccountItemPreview() {
    ExpenseManagerTheme {
        DashBoardAccountItem(
            modifier = Modifier
                .wrapContentWidth()
                .padding(16.dp),
            name = "Utilities is having a lengthy one",
            icon = "ic_calendar",
            amount = UiText.DynamicString("0.0$"),
            amountTextColor = colorResource(id = R.color.green_500),
            backgroundColor = colorResource(id = R.color.black_100),
        )
    }
}

@Preview
@Composable
private fun AccountItemPreview() {
    ExpenseManagerTheme {
        AccountItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            name = "Utilities",
            icon = "ic_calendar",
            iconBackgroundColor = "#000000",
            amount = "$100.00",
            endIcon = R.drawable.ic_arrow_right
        )
    }
}

@Preview
@Composable
private fun AccountCheckedItemPreview() {
    ExpenseManagerTheme {
        AccountCheckedItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            name = "First Account",
            icon = "savings",
            iconBackgroundColor = "#000000",
            isSelected = true
        )
    }
}

@Preview
@Composable
private fun AccountListItemLoadingStatePreview() {
    ExpenseManagerTheme {
        AccountListScreenScaffoldView(
            rememberNavController(),
            accountUiState = UiState.Loading,
        )
    }
}

@Preview
@Composable
private fun AccountListItemEmptyStatePreview() {
    ExpenseManagerTheme {
        AccountListScreenScaffoldView(
            rememberNavController(),
            accountUiState = UiState.Empty,
        )
    }
}

@Preview
@Composable
private fun AccountListItemSuccessStatePreview() {
    ExpenseManagerTheme {
        AccountListScreenScaffoldView(
            rememberNavController(),
            accountUiState = UiState.Success(
                ACCOUNT_DUMMY_DATA
            ),
        )
    }
}