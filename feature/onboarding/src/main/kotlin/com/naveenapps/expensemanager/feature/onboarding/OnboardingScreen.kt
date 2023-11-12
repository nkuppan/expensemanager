package com.naveenapps.expensemanager.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.currency.CurrencyDialogView

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {

    val currency by viewModel.currency.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showCurrencySelection by remember { mutableStateOf(false) }
    if (showCurrencySelection) {
        CurrencyDialogView {
            showCurrencySelection = false
        }
    }

    Scaffold {
        OnboardingContentView(
            modifier = Modifier.padding(it),
            currency = currency,
            accounts = accounts,
        ) { actionType ->
            when (actionType) {
                1 -> {
                    showCurrencySelection = true
                }

                2 -> {
                    navController.navigate("account/create")
                }

                3 -> {
                    navController.navigate("account/create")
                }

                4 -> {
                    navController.navigate("home") {
                        popUpTo("onboarding") {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingContentView(
    currency: Currency,
    accounts: List<AccountUiModel>,
    modifier: Modifier = Modifier,
    onSelection: (Int) -> Unit
) {
    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp)
                ) {
                    Image(
                        modifier = Modifier
                            .size(128.dp)
                            .align(Alignment.BottomCenter),
                        painter = painterResource(id = com.naveenapps.expensemanager.core.common.R.drawable.expenses),
                        contentDescription = null
                    )
                }
            }
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_message),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
            }

            item {
                Text(
                    modifier = Modifier
                        .padding(top = 32.dp),
                    text = stringResource(id = R.string.select_currency),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                CurrencyItem(
                    modifier = Modifier
                        .clickable {
                            onSelection.invoke(1)
                        }
                        .padding(top = 8.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .background(
                            colorResource(
                                id = com.naveenapps.expensemanager.core.common.R.color.black_100
                            ).copy(
                                alpha = 0.10f
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    title = stringResource(id = R.string.currency),
                    description = stringResource(id = currency.name),
                    icon = currency.icon
                )
            }

            item {
                Row {
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .weight(1f),
                        text = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                onSelection.invoke(2)
                            },
                        text = stringResource(id = R.string.add_new).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(
                accounts,
                key = {
                    it.id
                }
            ) { account ->
                AccountItem(
                    name = account.name,
                    icon = account.icon,
                    iconBackgroundColor = account.iconBackgroundColor,
                    amount = account.amount.amountString,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelection.invoke(3)
                        }
                        .padding(top = 16.dp)
                        .background(
                            colorResource(
                                id = com.naveenapps.expensemanager.core.common.R.color.black_100
                            ).copy(
                                alpha = 0.10f
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onClick = {
                    onSelection.invoke(4)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = stringResource(id = R.string.proceed).uppercase())
            }
        }
    }
}


@Composable
private fun CurrencyItem(
    title: String,
    description: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            text = "$title $description"
        )
        Icon(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 16.dp),
            imageVector = Icons.Default.ArrowRight,
            contentDescription = null
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun OnboardingScreenPreview() {
    ExpenseManagerTheme {
        Surface {
            OnboardingContentView(
                modifier = Modifier
                    .fillMaxSize(),
                currency = Currency(
                    com.naveenapps.expensemanager.core.common.R.string.dollar_type,
                    com.naveenapps.expensemanager.core.common.R.string.dollar_name,
                    com.naveenapps.expensemanager.core.common.R.drawable.currency_dollar
                ),
                accounts = listOf(
                    AccountUiModel(
                        id = "1",
                        name = "Shopping",
                        type = AccountType.REGULAR,
                        icon = "currency_dollar",
                        iconBackgroundColor = "#000000",
                        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                        amount = Amount(0.0, "$ 0.00"),
                    )
                )
            ) {

            }
        }
    }
}