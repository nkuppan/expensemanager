package com.naveenapps.expensemanager.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionDialog

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    OnboardingContentView(
        state = state,
        onAction = viewModel::processAction
    )
}

@Composable
private fun OnboardingContentView(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {

    if (state.showCurrencySelection) {
        CountryCurrencySelectionDialog { country ->
            onAction.invoke(OnboardingAction.SelectCurrency(country?.currency))
        }
    }
    Scaffold(
        topBar = {
            AppTopNavigationBar(
                title = "",
                navigationIcon = Icons.Default.Close,
                navigationBackClick = {
                    onAction.invoke(OnboardingAction.Next)
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Text(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_message),
                    style = MaterialTheme.typography.titleLarge,
                )

                ClickableTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    label = R.string.select_currency,
                    value = state.currency.name.ifBlank { stringResource(id = R.string.currency) },
                    onClick = { onAction.invoke(OnboardingAction.ShowCurrencySelection) },
                    trailingIcon = Icons.AutoMirrored.Filled.ArrowRight
                )

                DashboardWidgetTitle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                    onViewAllClick = {
                        onAction.invoke(OnboardingAction.AccountCreate(null))
                    },
                )
                state.accounts.forEach { account ->
                    AccountItem(
                        name = account.name,
                        icon = account.storedIcon.name,
                        iconBackgroundColor = account.storedIcon.backgroundColor,
                        amount = account.amount.amountString,
                        amountTextColor = account.amountTextColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAction.invoke(OnboardingAction.AccountCreate(account))
                            }
                            .padding(16.dp),
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    onClick = {
                        onAction.invoke(OnboardingAction.Next)
                    },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(text = stringResource(id = R.string.proceed).uppercase())
                }
            }
        }
    }
}

@Composable
private fun CurrencyItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
) {
    Row(modifier = modifier) {
        if (icon != null) {
            Icon(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp),
                painter = painterResource(id = icon),
                contentDescription = null,
            )
        }
        Text(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            text = "$title $description",
        )
        Icon(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 16.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = null,
        )
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun OnboardingScreenPreview() {
    ExpenseManagerTheme {
        OnboardingContentView(
            state = OnboardingState(
                currency = Currency("1", "1"),
                accounts = listOf(
                    AccountUiModel(
                        id = "1",
                        name = "Shopping",
                        type = AccountType.REGULAR,
                        storedIcon = StoredIcon(
                            name = "account_balance",
                            backgroundColor = "#000000",
                        ),
                        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                        amount = Amount(0.0, "$ 0.00"),
                    ),
                ),
                showCurrencySelection = false
            ),
            onAction = {}
        )
    }
}
