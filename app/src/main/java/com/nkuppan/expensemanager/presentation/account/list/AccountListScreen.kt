package com.nkuppan.expensemanager.presentation.account.list

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.core.ui.extensions.getDrawable
import com.nkuppan.expensemanager.core.ui.theme.ExpenseManagerTheme
import com.nkuppan.expensemanager.core.ui.theme.NavigationButton
import com.nkuppan.expensemanager.core.ui.utils.UiText
import com.nkuppan.expensemanager.domain.model.AccountType
import com.nkuppan.expensemanager.domain.model.UiState
import kotlinx.coroutines.launch


@Composable
fun AccountListScreen(
    navController: NavController
) {
    val viewModel: AccountListViewModel = hiltViewModel()
    val accountUiState by viewModel.accounts.collectAsState()
    AccountListScreenScaffoldView(navController, accountUiState)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountListScreenScaffoldView(
    navController: NavController,
    accountUiState: UiState<List<AccountUiModel>>
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NavigationButton(navController)
                },
                title = {
                    Text(text = stringResource(R.string.account))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("account/create")
            }) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) { innerPadding ->

        val message = stringResource(id = R.string.cannot_edit_cash_account)

        AccountListScreenContent(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            accountUiState = accountUiState
        ) { account ->
            if (account.type != AccountType.CASH) {
                navController.navigate("account/create?accountId=${account.id}")
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
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
                                .fillMaxWidth()
                                .clickable {
                                    onItemClick?.invoke(account)
                                }
                                .padding(16.dp),
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
    val context = LocalContext.current

    Row(modifier = modifier) {
        Box(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(36.dp)
                .align(Alignment.CenterVertically),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                onDraw = {
                    drawCircle(
                        color = Color(
                            android.graphics.Color.parseColor(
                                iconBackgroundColor
                            )
                        )
                    )
                }
            )
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.Center),
                painter = painterResource(id = context.getDrawable(icon)),
                colorFilter = ColorFilter.tint(color = Color.White),
                contentDescription = name
            )
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = name
        )
        if (amount != null) {
            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = amount,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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

val DUMMY_DATA = listOf(
    AccountUiModel(
        id = "1",
        name = "Cash",
        icon = "ic_account",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
    ),
    AccountUiModel(
        id = "2",
        name = "Bank Account - xxxx",
        icon = "ic_account_balance",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
    ),
    AccountUiModel(
        id = "3",
        name = "Credit Card - xxxx",
        icon = "credit_card",
        iconBackgroundColor = "#000000",
        amount = UiText.DynamicString("$100.00")
    ),
)

@Preview
@Composable
private fun AccountItemPreview() {
    ExpenseManagerTheme {
        AccountItem(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                DUMMY_DATA
            ),
        )
    }
}