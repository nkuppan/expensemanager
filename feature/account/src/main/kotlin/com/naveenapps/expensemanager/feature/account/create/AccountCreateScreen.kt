package com.naveenapps.expensemanager.feature.account.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.IconAndColorComponent
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountField
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmDeleteConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPickerField
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.utils.rememberImagePickerActions
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionBottomSheet
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AccountCreateScreen(
    viewModel: AccountCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    ObserveAsEvents(viewModel.errorMessage) { messageRes ->
        coroutineScope.launch { snackbarHostState.showSnackbar(context.getString(messageRes)) }
    }

    val imagePicker = rememberImagePickerActions(
        createCaptureUri = viewModel::createImageCaptureUri,
        onImagePicked = { viewModel.processAction(AccountCreateAction.ImagePicked(it)) },
    )

    AccountCreateScaffoldView(
        state = state,
        onAction = viewModel::processAction,
        snackbarHostState = snackbarHostState,
        onCaptureRequested = imagePicker.onCaptureRequested,
        onGalleryRequested = imagePicker.onGalleryRequested,
    )
}

@Composable
private fun AccountCreateScaffoldView(
    state: AccountCreateState,
    onAction: (AccountCreateAction) -> Unit,
    onCaptureRequested: () -> Unit = {},
    onGalleryRequested: () -> Unit = {},
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    if (state.showDeleteDialog) {
        val impact = state.deleteImpact
        if (impact?.isLastAccount == true) {
            EmConfirm(
                title = stringResource(R.string.account_delete_last_title),
                message = stringResource(R.string.account_delete_last_message),
                confirmText = stringResource(DesignSystemR.string.ok),
                onConfirm = { onAction.invoke(AccountCreateAction.DismissDeleteDialog) },
                onDismiss = { onAction.invoke(AccountCreateAction.DismissDeleteDialog) },
            )
        } else {
            EmDeleteConfirm(
                onConfirm = { onAction.invoke(AccountCreateAction.Delete) },
                onDismiss = { onAction.invoke(AccountCreateAction.DismissDeleteDialog) },
                message = when {
                    impact == null -> null

                    impact.transactionCount > 0 -> pluralStringResource(
                        R.plurals.account_delete_message_with_transactions,
                        impact.transactionCount,
                        impact.transactionCount,
                    )

                    else -> stringResource(R.string.account_delete_message_no_transactions)
                },
            )
        }
    }

    if (state.showCurrencySelection) {
        CountryCurrencySelectionBottomSheet(
            onEvent = { event ->
                when (event) {
                    CountrySelectionEvent.Dismiss ->
                        onAction.invoke(AccountCreateAction.DismissCurrencySelection)

                    is CountrySelectionEvent.CountrySelected ->
                        onAction.invoke(AccountCreateAction.SelectCurrency(event.country))
                }
            },
        )
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(AccountCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton) {
                    stringResource(R.string.edit_account)
                } else {
                    stringResource(R.string.create_account)
                },
                actions = {
                    if (state.showDeleteButton && state.hasTransactions) {
                        // An account with history can't be deleted, only archived (reversible).
                        EmIconButton(
                            icon = Icons.Outlined.Archive,
                            contentDescription = stringResource(R.string.archive_account),
                            onClick = { onAction.invoke(AccountCreateAction.Archive) },
                        )
                    } else if (state.showDeleteButton) {
                        EmIconButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                            onClick = { onAction.invoke(AccountCreateAction.ShowDeleteDialog) },
                            tint = EmTheme.colors.scheme.error,
                        )
                    }
                },
            )
        },
        bottomBar = {
            EmBottomActionBar(
                primaryText = stringResource(R.string.save),
                onPrimaryClick = { onAction.invoke(AccountCreateAction.Save) },
                primaryIcon = Icons.Default.Done,
            )
        },
    ) { innerPadding ->
        AccountCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            name = state.name,
            amount = state.amount,
            type = state.type,
            color = state.color,
            icon = state.icon,
            creditLimit = state.creditLimit,
            totalAmount = state.totalAmount,
            totalAmountBackgroundColor = state.totalAmountBackgroundColor,
            customImagePath = state.customImagePath,
            accountCurrency = state.accountCurrency,
            accountCurrencyError = state.accountCurrencyError,
            isEditMode = state.showDeleteButton,
            onCaptureRequested = onCaptureRequested,
            onGalleryRequested = onGalleryRequested,
            onRemoveImage = { onAction.invoke(AccountCreateAction.RemoveImage) },
            onCurrencyClick = { onAction.invoke(AccountCreateAction.OpenCurrencySelection) },
        )
    }
}

@Composable
private fun AccountCreateScreen(
    modifier: Modifier = Modifier,
    name: TextFieldValue<String>,
    amount: TextFieldValue<String>,
    type: TextFieldValue<AccountType>,
    color: TextFieldValue<String>,
    icon: TextFieldValue<String>,
    creditLimit: TextFieldValue<String>,
    totalAmount: String,
    totalAmountBackgroundColor: Int,
    customImagePath: String? = null,
    accountCurrency: Currency?,
    accountCurrencyError: Boolean = false,
    isEditMode: Boolean = false,
    onCaptureRequested: () -> Unit = {},
    onGalleryRequested: () -> Unit = {},
    onRemoveImage: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        if (isEditMode) {
            LockedAccountDetails(
                name = name.value,
                type = type.value,
                currency = accountCurrency,
                totalAmount = totalAmount,
                modifier = Modifier.padding(top = EmTheme.spacing.sm),
            )
        } else {
            EmSection(
                title = stringResource(R.string.account_type),
                modifier = Modifier.padding(top = EmTheme.spacing.sm),
            ) {
                EmCard {
                    Column(
                        modifier = Modifier.padding(EmTheme.spacing.lg),
                    ) {
                        AccountTypeSelectionView(
                            modifier = Modifier.fillMaxWidth(),
                            selectedAccountType = type.value,
                            onAccountTypeChange = type.onValueChange!!,
                        )
                    }
                }
            }

            // Details
            EmSection(title = stringResource(R.string.details)) {
                EmCard {
                    Column(
                        modifier = Modifier.padding(EmTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                    ) {
                        EmTextField(
                            value = name.value,
                            onValueChange = { name.onValueChange?.invoke(it) },
                            label = stringResource(id = R.string.account_name),
                            modifier = Modifier.fillMaxWidth(),
                            isError = name.valueError,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            supportingText = if (name.valueError) stringResource(id = R.string.account_name_error) else null,
                            selectAllOnFocus = true,
                        )
                        EmPickerField(
                            label = stringResource(id = R.string.currency),
                            value = accountCurrency?.toDisplayValue()
                                ?: stringResource(id = R.string.currency_not_set),
                            onClick = onCurrencyClick,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = Icons.Outlined.Paid,
                        )
                        if (accountCurrencyError) {
                            EmText(
                                text = stringResource(id = R.string.currency_error),
                                role = EmTextRole.Supporting,
                                color = EmTextColor.Error,
                            )
                        }
                        BalanceSectionContent(
                            amount = amount,
                            type = type,
                            creditLimit = creditLimit,
                            totalAmount = totalAmount,
                            totalAmountBackgroundColor = totalAmountBackgroundColor,
                        )
                    }
                }
            }
        }

        EmSection(title = stringResource(R.string.appearance)) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                ) {
                    IconAndColorComponent(
                        modifier = Modifier.fillMaxWidth(),
                        selectedColor = color.value,
                        selectedIcon = icon.value,
                        onColorSelection = color.onValueChange,
                        onIconSelection = icon.onValueChange,
                        customImagePath = customImagePath,
                        onCaptureRequested = onCaptureRequested,
                        onGalleryRequested = onGalleryRequested,
                        onRemoveImage = onRemoveImage,
                    )
                }
            }
        }

        // FAB clearance
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun ColumnScope.BalanceSectionContent(
    amount: TextFieldValue<String>,
    type: TextFieldValue<AccountType>,
    creditLimit: TextFieldValue<String>,
    totalAmount: String,
    totalAmountBackgroundColor: Int,
) {
    EmAmountField(
        value = amount.value,
        onValueChange = { amount.onValueChange?.invoke(it) },
        label = stringResource(id = R.string.current_balance),
        modifier = Modifier.fillMaxWidth(),
        isError = amount.valueError,
        errorMessage = stringResource(id = R.string.current_balance_error),
        imeAction = ImeAction.Next,
    )

    // Credit limit — only for credit accounts
    AnimatedVisibility(
        visible = type.value == AccountType.CREDIT,
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
    ) {
        EmAmountField(
            value = creditLimit.value,
            onValueChange = { creditLimit.onValueChange?.invoke(it) },
            label = stringResource(id = R.string.credit_limit),
            modifier = Modifier.fillMaxWidth(),
            isError = creditLimit.valueError,
            errorMessage = stringResource(id = R.string.credit_limit_error),
            imeAction = ImeAction.Done,
        )
    }

    // Available balance summary
    AnimatedVisibility(
        visible = totalAmount.isNotBlank(),
        enter = fadeIn(tween(200)) + expandVertically(tween(250)),
        exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = EmTheme.spacing.md)
                .clip(EmTheme.shapes.md)
                .background(emColorOf(totalAmountBackgroundColor).tint()),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EmIcon(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    size = EmTheme.icons.md,
                    tint = emColorOf(totalAmountBackgroundColor),
                )
                Spacer(modifier = Modifier.width(EmTheme.spacing.md))
                EmText(
                    text = stringResource(id = R.string.available_balance),
                    modifier = Modifier.weight(1f),
                )
                EmText(
                    text = totalAmount,
                    role = EmTextRole.SectionTitle,
                    color = emColorOf(totalAmountBackgroundColor),
                )
            }
        }
    }
}

@EmPreviews
@Composable
private fun AccountCreateStatePreview() {
    val nameField = TextFieldValue(
        value = "",
        valueError = false,
        onValueChange = { },
    )
    val selectedColorField = TextFieldValue(
        value = "#000000",
        valueError = false,
        onValueChange = { },
    )
    val selectedIconField = TextFieldValue(
        value = "account_balance_wallet",
        valueError = false,
        onValueChange = { },
    )
    val accountField = TextFieldValue(
        value = AccountType.CREDIT,
        valueError = false,
        onValueChange = { },
    )
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AccountCreateScaffoldView(
            state = AccountCreateState(
                name = nameField,
                type = accountField,
                color = selectedColorField,
                icon = selectedIconField,
                creditLimit = nameField,
                currency = Currency("$", ""),
                accountCurrency = Currency("$", ""),
                totalAmount = "$ 0.0",
                totalAmountBackgroundColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                amount = nameField,
                showDeleteButton = false,
                showDeleteDialog = false,
            ),
            onAction = {},
        )
    }
}

/**
 * An existing account's identity is immutable: name, type, currency and balance are shown
 * read-only (the balance only moves through transactions). Only the appearance is editable.
 */
@Composable
private fun LockedAccountDetails(
    name: String,
    type: AccountType,
    currency: Currency?,
    totalAmount: String,
    modifier: Modifier = Modifier,
) {
    EmSection(title = stringResource(R.string.fixed_details), modifier = modifier) {
        EmCard {
            Column(
                modifier = Modifier.padding(EmTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                LockedDetailRow(stringResource(R.string.account_name), name)
                LockedDetailRow(
                    stringResource(R.string.account_type),
                    stringResource(
                        if (type == AccountType.CREDIT) R.string.credit else R.string.bank_account,
                    ),
                )
                LockedDetailRow(
                    stringResource(R.string.currency),
                    currency?.toDisplayValue() ?: stringResource(R.string.currency_not_set),
                )
                LockedDetailRow(stringResource(R.string.current_balance), totalAmount)
                EmText(
                    text = stringResource(R.string.fixed_details_hint),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

@Composable
private fun LockedDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        EmText(text = label, color = EmTextColor.Supporting)
        EmText(text = value)
    }
}
