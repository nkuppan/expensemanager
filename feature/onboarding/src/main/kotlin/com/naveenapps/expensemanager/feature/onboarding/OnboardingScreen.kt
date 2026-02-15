package com.naveenapps.expensemanager.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.PrimaryButton
import com.naveenapps.expensemanager.core.designsystem.ui.components.PrimaryOutlinedButton
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.feature.account.list.AccountItem
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionDialog
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel()
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
        CountryCurrencySelectionDialog(
            onEvent = { event ->
                when (event) {
                    CountrySelectionEvent.Dismiss -> {
                        onAction.invoke(OnboardingAction.DismissCurrencySelection)
                    }

                    is CountrySelectionEvent.CountrySelected -> {
                        onAction.invoke(OnboardingAction.SelectCurrency(event.country))
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        text = stringResource(id = R.string.setup),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    ClickableTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                        label = R.string.select_main_currency,
                        value = state.currency.name.ifBlank { stringResource(id = R.string.currency) },
                        onClick = { onAction.invoke(OnboardingAction.ShowCurrencySelection) },
                        trailingIcon = Icons.AutoMirrored.Filled.ArrowRight
                    )

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
                        style = MaterialTheme.typography.headlineSmall,
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

                    PrimaryOutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = {
                            onAction.invoke(OnboardingAction.AccountCreate(null))
                        }
                    ) {
                        Text(text = stringResource(id = R.string.create_new).uppercase())
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                PrimaryButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    onClick = {
                        onAction.invoke(OnboardingAction.Next)
                    },
                ) {
                    Text(text = stringResource(id = R.string.proceed).uppercase())
                }
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun OnboardingScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
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
