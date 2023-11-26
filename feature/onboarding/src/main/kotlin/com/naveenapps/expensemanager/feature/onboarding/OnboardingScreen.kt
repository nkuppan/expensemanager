package com.naveenapps.expensemanager.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.currency.CurrencyDialogView

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {

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
        ) { actionType, id ->
            when (actionType) {
                1 -> {
                    showCurrencySelection = true
                }

                2 -> {
                    viewModel.openAccountCreateScreen(id)
                }

                3 -> {
                    viewModel.openHome()
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
    onSelection: (Int, String?) -> Unit
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    modifier = Modifier
                        .padding(64.dp)
                        .size(128.dp)
                        .align(Alignment.BottomCenter),
                    painter = painterResource(id = com.naveenapps.expensemanager.core.common.R.drawable.expenses),
                    contentDescription = null
                )
            }
            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.welcome_message),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                title = stringResource(id = R.string.select_currency),
            )

            CurrencyItem(
                modifier = Modifier
                    .clickable {
                        onSelection.invoke(1, null)
                    }
                    .fillMaxWidth(),
                title = stringResource(id = R.string.currency),
                description = stringResource(id = currency.name),
                icon = currency.symbolIcon
            )
            DashboardWidgetTitle(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                onViewAllClick = {
                    onSelection.invoke(2, null)
                },
            )
            accounts.forEach { account ->
                AccountItem(
                    name = account.name,
                    icon = account.storedIcon.name,
                    iconBackgroundColor = account.storedIcon.backgroundColor,
                    amount = account.amount.amountString,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelection.invoke(2, account.id)
                        }
                        .padding(16.dp),
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
                    onSelection.invoke(3, null)
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
            imageVector = Icons.Filled.ArrowRight,
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
                currency = Currency(1, 1, 1),
                accounts = listOf(
                    AccountUiModel(
                        id = "1",
                        name = "Shopping",
                        type = AccountType.REGULAR,
                        storedIcon = StoredIcon(
                            name = "currency_dollar",
                            backgroundColor = "#000000"
                        ),
                        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                        amount = Amount(0.0, "$ 0.00"),
                    )
                )
            ) { type, id ->

            }
        }
    }
}