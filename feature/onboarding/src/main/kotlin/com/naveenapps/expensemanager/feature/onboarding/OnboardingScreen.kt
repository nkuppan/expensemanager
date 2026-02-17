package com.naveenapps.expensemanager.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import com.naveenapps.invoicebuilder.feature.country.CountryCurrencySelectionBottomSheet
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
        CountryCurrencySelectionBottomSheet(
            onEvent = { event ->
                when (event) {
                    CountrySelectionEvent.Dismiss -> {
                        onAction.invoke(OnboardingAction.DismissCurrencySelection)
                    }

                    is CountrySelectionEvent.CountrySelected -> {
                        onAction.invoke(OnboardingAction.SelectCurrency(event.country))
                    }
                }
            },
        )
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = "",
                navigationIcon = Icons.Default.Close,
                navigationBackClick = {
                    onAction.invoke(OnboardingAction.Next)
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { onAction.invoke(OnboardingAction.Next) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(
                        text = stringResource(id = R.string.proceed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.setup),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.3).sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = R.string.setup_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Currency
            StepSection(
                title = stringResource(id = R.string.select_main_currency),
            ) {
                val currencyName = state.currency.name
                AppCardView(
                    onClick = { onAction.invoke(OnboardingAction.ShowCurrencySelection) },
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.setup_currency),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (currencyName.isNotBlank())
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                            if (currencyName.isNotBlank()) {
                                Text(
                                    text = stringResource(id = R.string.tap_to_change),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.not_set_up_yet),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                        // Value and chevron
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.currency.toDisplayValue().isNotBlank()) {
                                Text(
                                    text = state.currency.toDisplayValue(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 2: Accounts
            StepSection(
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    state.accounts.forEachIndexed { index, account ->
                        val shape = when {
                            state.accounts.size == 1 -> MaterialTheme.shapes.medium
                            index == 0 -> RoundedCornerShape(
                                topStart = 12.dp, topEnd = 12.dp,
                                bottomStart = 4.dp, bottomEnd = 4.dp,
                            )

                            index == state.accounts.lastIndex -> RoundedCornerShape(
                                topStart = 4.dp, topEnd = 4.dp,
                                bottomStart = 12.dp, bottomEnd = 12.dp,
                            )

                            else -> RoundedCornerShape(4.dp)
                        }

                        AccountItem(
                            onClick = { onAction.invoke(OnboardingAction.AccountCreate(account)) },
                            name = account.name,
                            icon = account.storedIcon.name,
                            iconBackgroundColor = account.storedIcon.backgroundColor,
                            amount = account.amount.amountString,
                            amountTextColor = account.amountTextColor,
                            modifier = Modifier,
                            shape = shape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onAction.invoke(OnboardingAction.AccountCreate(null)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.create_new),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StepSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        content()
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun OnboardingScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        OnboardingContentView(
            state = OnboardingState(
                currency = Currency("$", "US Dollars", code = "USD"),
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
                    AccountUiModel(
                        id = "2",
                        name = "Shopping",
                        type = AccountType.REGULAR,
                        storedIcon = StoredIcon(
                            name = "account_balance",
                            backgroundColor = "#000000",
                        ),
                        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                        amount = Amount(0.0, "$ 0.00"),
                    ),
                    AccountUiModel(
                        id = "3",
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
