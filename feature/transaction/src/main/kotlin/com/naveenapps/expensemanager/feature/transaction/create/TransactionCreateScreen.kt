package com.naveenapps.expensemanager.feature.transaction.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naveenapps.expensemanager.core.common.utils.hourAndMinute
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.common.utils.withTime
import com.naveenapps.expensemanager.core.designsystem.R as DesignSystemR
import com.naveenapps.expensemanager.core.designsystem.components.AttachmentAddTile
import com.naveenapps.expensemanager.core.designsystem.components.AttachmentPickerScreen
import com.naveenapps.expensemanager.core.designsystem.components.AttachmentThumbnail
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountField
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmBottomActionBar
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmChip
import com.naveenapps.expensemanager.core.designsystem.em.EmConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmDeleteConfirm
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmModalSheet
import com.naveenapps.expensemanager.core.designsystem.em.EmPickerField
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSection
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmSnackbarHostState
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextField
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.utils.displayName
import com.naveenapps.expensemanager.core.designsystem.ui.utils.rememberImagePickerActions
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.domain.usecase.recurring.isOnLaterDay
import com.naveenapps.expensemanager.core.domain.usecase.recurring.occurrenceDate
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.RecurringFrequency
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.selection.CategoryItem
import com.naveenapps.expensemanager.feature.category.selection.CategoryItemDefaults
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.feature.transaction.R
import com.naveenapps.expensemanager.feature.transaction.create.receipt.ScanReceiptChip
import com.naveenapps.expensemanager.feature.transaction.create.receipt.SuggestedCategoryRow
import com.naveenapps.expensemanager.feature.transaction.create.receipt.rememberReceiptScanner
import com.naveenapps.expensemanager.feature.transaction.numberpad.NumberPadDialogView
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionCreateScreen(
    shareRepository: ShareRepository,
    viewModel: TransactionCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { EmSnackbarHostState() }
    val resources = LocalContext.current.resources

    ObserveAsEvents(viewModel.event) {
        when (it) {
            TransactionCreateEvent.RequestReview -> {
                coroutineScope.launch {
                    shareRepository.requestInAppReview()
                }
            }

            is TransactionCreateEvent.ShowMessage -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(resources.getString(it.messageRes))
                }
            }
        }
    }

    val attachmentPicker = rememberImagePickerActions(
        createCaptureUri = viewModel::createImageCaptureUri,
        onImagePicked = { viewModel.processAction(TransactionCreateAction.AttachmentPicked(it)) },
    )

    val openReceiptScanner = rememberReceiptScanner(
        onScanned = { viewModel.processAction(TransactionCreateAction.ReceiptScanned(it)) },
        onPreparing = { viewModel.processAction(TransactionCreateAction.ReceiptScannerPreparing) },
        onFailed = { viewModel.processAction(TransactionCreateAction.ReceiptScanFailed) },
    )

    TransactionCreateScreenContent(
        state = state,
        onAction = viewModel::processAction,
        onCaptureRequested = attachmentPicker.onCaptureRequested,
        onGalleryRequested = attachmentPicker.onGalleryRequested,
        onScanReceipt = {
            viewModel.processAction(TransactionCreateAction.ReceiptScanRequested)
            openReceiptScanner()
        },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun TransactionCreateScreenContent(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
    onCaptureRequested: () -> Unit = {},
    onGalleryRequested: () -> Unit = {},
    onScanReceipt: () -> Unit = {},
    snackbarHostState: EmSnackbarHostState = remember { EmSnackbarHostState() },
) {
    // System back goes through the same path as the top-bar back arrow, so unsaved edits get a
    // "Discard changes?" prompt and attachments added in this session are cleaned up.
    BackHandler { onAction.invoke(TransactionCreateAction.ClosePage) }

    if (state.showDiscardDialog) {
        EmConfirm(
            title = stringResource(R.string.discard_changes_title),
            message = stringResource(R.string.discard_changes_message),
            confirmText = stringResource(R.string.discard),
            onConfirm = { onAction.invoke(TransactionCreateAction.ConfirmDiscard) },
            onDismiss = { onAction.invoke(TransactionCreateAction.DismissDiscardDialog) },
            dismissText = stringResource(R.string.keep_editing),
            destructive = true,
        )
    } else if (state.showDeleteDialog) {
        EmDeleteConfirm(
            onConfirm = { onAction.invoke(TransactionCreateAction.Delete) },
            onDismiss = { onAction.invoke(TransactionCreateAction.DismissDeleteDialog) },
        )
    } else if (state.showNumberPad) {
        NumberPadDialogView(
            initialAmount = state.numberPadInitialAmount,
            onConfirm = { amount ->
                onAction.invoke(TransactionCreateAction.SetNumberPadValue(amount))
            },
            header = if (state.quickPicks.isEmpty() && !state.canScanReceipt) {
                null
            } else {
                {
                    Column(modifier = Modifier.padding(top = EmTheme.spacing.lg)) {
                        if (state.canScanReceipt) {
                            ScanReceiptChip(
                                modifier = Modifier.padding(horizontal = EmTheme.spacing.lg),
                                isReading = state.isReadingReceipt,
                                onClick = onScanReceipt,
                            )
                        }
                        if (state.quickPicks.isNotEmpty()) {
                            QuickPickRow(
                                picks = state.quickPicks,
                                onPick = { onAction.invoke(TransactionCreateAction.ApplyQuickPick(it)) },
                                contentPadding = PaddingValues(horizontal = EmTheme.spacing.lg),
                            )
                        }
                    }
                }
            },
        )
    } else if (state.showCategorySelection) {
        CategorySelectionView(state, onAction)
    } else if (state.showAccountSelection) {
        AccountSelectionView(state, onAction)
    } else if (state.showAttachmentPicker) {
        AttachmentPickerView(onAction, onCaptureRequested, onGalleryRequested)
    }

    EmScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            EmTopBar(
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    onAction.invoke(TransactionCreateAction.ClosePage)
                },
                title = if (state.showDeleteButton) {
                    stringResource(R.string.edit_transaction)
                } else {
                    stringResource(R.string.create_transaction)
                },
                actions = {
                    if (state.showDeleteButton) {
                        EmIconButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                            onClick = { onAction.invoke(TransactionCreateAction.ShowDeleteDialog) },
                            tint = EmTheme.colors.scheme.error,
                        )
                    }
                },
            )
        },
        bottomBar = {
            EmBottomActionBar(
                primaryText = stringResource(R.string.save),
                onPrimaryClick = { onAction.invoke(TransactionCreateAction.Save) },
                primaryIcon = Icons.Default.Done,
            )
        },
    ) { innerPadding ->
        TransactionCreateScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            state = state,
            onAction = onAction,
            onScanReceipt = onScanReceipt,
        )
    }
}

@Composable
private fun AttachmentPickerView(
    onAction: (TransactionCreateAction) -> Unit,
    onCaptureRequested: () -> Unit,
    onGalleryRequested: () -> Unit,
) {
    EmModalSheet(
        onDismissRequest = {
            onAction.invoke(TransactionCreateAction.DismissAttachmentPicker)
        },
    ) {
        AttachmentPickerScreen(
            onTakePhoto = {
                onAction.invoke(TransactionCreateAction.DismissAttachmentPicker)
                onCaptureRequested()
            },
            onChooseFromGallery = {
                onAction.invoke(TransactionCreateAction.DismissAttachmentPicker)
                onGalleryRequested()
            },
        )
    }
}

@Composable
private fun AccountSelectionView(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
) {
    EmModalSheet(
        onDismissRequest = {
            onAction.invoke(TransactionCreateAction.DismissAccountSelection)
        },
    ) {
        AccountSelectionScreen(
            accounts = state.accounts,
            selectedAccount = when (state.accountSelection) {
                AccountSelection.FROM_ACCOUNT -> state.selectedFromAccount
                AccountSelection.TO_ACCOUNT -> state.selectedToAccount
            },
            createNewCallback = {
                onAction.invoke(TransactionCreateAction.OpenAccountCreate(null))
            },
            onItemSelection = {
                onAction.invoke(TransactionCreateAction.SelectAccount(it))
            },
        )
    }
}

@Composable
private fun CategorySelectionView(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
) {
    EmModalSheet(
        onDismissRequest = {
            onAction.invoke(TransactionCreateAction.DismissCategorySelection)
        },
    ) {
        CategorySelectionScreen(
            categories = state.categories,
            selectedCategory = state.selectedCategory,
            createNewCallback = {
                onAction.invoke(TransactionCreateAction.OpenCategoryCreate(null))
            },
            onItemSelection = {
                onAction.invoke(TransactionCreateAction.SelectCategory(it))
            },
        )
    }
}

@Composable
private fun TransactionCreateScreen(
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
    modifier: Modifier = Modifier,
    onScanReceipt: () -> Unit = {},
) {
    if (state.showDateSelection) {
        AppDatePickerDialog(
            selectedDate = state.dateTime,
            onDateSelected = {
                onAction.invoke(TransactionCreateAction.SelectDate(it))
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            },
        )
    }

    if (state.showTimeSelection) {
        val reminder = state.dateTime.toTime()
        AppTimePickerDialog(
            reminderTimeState = Triple(reminder.hour, reminder.minute, reminder.is24Hour),
            onTimeSelected = {
                val reminderTimeState = ReminderTimeState(it.first, it.second, it.third)
                onAction.invoke(
                    TransactionCreateAction.SelectDate(
                        state.dateTime.toTime(reminderTimeState),
                    ),
                )
            },
            onDismiss = {
                onAction.invoke(TransactionCreateAction.DismissDateSelection)
            },
        )
    }

    TransactionCreateContent(
        modifier = modifier,
        state = state,
        onAction = onAction,
        onScanReceipt = onScanReceipt,
    )
}

@Composable
private fun TransactionCreateContent(
    modifier: Modifier,
    state: TransactionCreateState,
    onAction: (TransactionCreateAction) -> Unit,
    onScanReceipt: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.lg),
    ) {
        EmSection(
            title = stringResource(R.string.transaction_type),
            modifier = Modifier.padding(top = EmTheme.spacing.sm),
        ) {
            EmCard {
                Column(
                    modifier = Modifier.padding(EmTheme.spacing.lg),
                ) {
                    TransactionTypeSelectionView(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTransactionType = state.transactionType,
                        onTransactionTypeChange = {
                            onAction.invoke(TransactionCreateAction.ChangeTransactionType(it))
                        },
                    )
                }
            }
        }

        EmSection(title = stringResource(R.string.details)) {
            EmCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(EmTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xs),
                ) {
                    EmAmountField(
                        value = state.amount.value,
                        onValueChange = { state.amount.onValueChange?.invoke(it) },
                        label = stringResource(id = R.string.amount),
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.amount.valueError,
                        errorMessage = stringResource(id = R.string.amount_error_message),
                        trailing = {
                            EmIconButton(
                                icon = Icons.Outlined.Calculate,
                                contentDescription = stringResource(DesignSystemR.string.a11y_open_calculator),
                                onClick = { onAction.invoke(TransactionCreateAction.ShowNumberPad) },
                            )
                        },
                    )

                    if (state.canScanReceipt) {
                        ScanReceiptChip(
                            isReading = state.isReadingReceipt,
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onScanReceipt()
                            },
                        )
                    }

                    if (state.quickPicks.isNotEmpty()) {
                        QuickPickRow(
                            modifier = Modifier.padding(vertical = EmTheme.spacing.xs),
                            picks = state.quickPicks,
                            onPick = {
                                focusManager.clearFocus(force = true)
                                onAction.invoke(TransactionCreateAction.ApplyQuickPick(it))
                            },
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.md),
                    ) {
                        EmPickerField(
                            label = stringResource(id = R.string.select_date),
                            value = state.dateTime.toCompleteDateWithDate(),
                            onClick = {
                                focusManager.clearFocus(force = true)
                                onAction.invoke(TransactionCreateAction.ShowDateSelection)
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.EditCalendar,
                        )
                        EmPickerField(
                            label = stringResource(id = R.string.select_time),
                            value = state.dateTime.toTimeAndMinutes(),
                            onClick = {
                                onAction.invoke(TransactionCreateAction.ShowTimeSelection)
                            },
                            modifier = Modifier.weight(1f),
                            leadingIcon = Icons.Outlined.AccessTime,
                        )
                    }

                    EmTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.notes.value,
                        singleLine = false,
                        maxLines = 3,
                        leadingIcon = Icons.AutoMirrored.Filled.Notes,
                        placeholder = stringResource(id = R.string.optional_details),
                        label = stringResource(id = R.string.notes),
                        onValueChange = {
                            state.notes.onValueChange?.invoke(it)
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus(force = true)
                            },
                        ),
                    )
                }
            }
        }

        state.recurringRuleId?.let {
            EmSection(title = stringResource(R.string.repeat)) {
                EmCard {
                    EmSettingItem(
                        icon = Icons.Outlined.EventRepeat,
                        title = stringResource(R.string.recurring_part_of_series),
                        supporting = stringResource(R.string.recurring_edit_series),
                        onClick = { onAction.invoke(TransactionCreateAction.OpenRecurringSeries) },
                    )
                }
            }
        }

        if (state.canRepeat) {
            EmSection(title = stringResource(R.string.repeat)) {
                RepeatSelector(
                    selected = state.repeat,
                    startDate = state.dateTime,
                    onSelect = { onAction.invoke(TransactionCreateAction.SelectRepeat(it)) },
                )
            }
        }

        // Category — only for non-transfer
        AnimatedVisibility(
            visible = state.transactionType != TransactionType.TRANSFER,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            EmSection(title = stringResource(R.string.select_category)) {
                Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.md)) {
                    CategoryItem(
                        name = state.selectedCategory.displayName(),
                        icon = state.selectedCategory.storedIcon.name,
                        iconBackgroundColor = state.selectedCategory.storedIcon.backgroundColor,
                        customImagePath = state.selectedCategory.storedIcon.customImagePath,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onAction.invoke(TransactionCreateAction.ShowCategorySelection)
                        },
                        trailingContent = {
                            CategoryItemDefaults.ChevronTrailing()
                        },
                    )
                    if (state.suggestedCategories.isNotEmpty()) {
                        SuggestedCategoryRow(
                            categories = state.suggestedCategories,
                            selected = state.selectedCategory,
                            onSelect = { onAction.invoke(TransactionCreateAction.SelectCategory(it)) },
                        )
                    }
                }
            }
        }

        EmSection(
            title = stringResource(
                id = if (state.transactionType == TransactionType.TRANSFER) {
                    R.string.from_account
                } else {
                    R.string.select_account
                },
            ),
        ) {
            AccountItem(
                name = state.selectedFromAccount.name,
                icon = state.selectedFromAccount.storedIcon.name,
                iconBackgroundColor = state.selectedFromAccount.storedIcon.backgroundColor,
                amount = state.selectedFromAccount.amount.amountString,
                amountTone = state.selectedFromAccount.amountTone,
                customImagePath = state.selectedFromAccount.storedIcon.customImagePath,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus(force = true)
                    onAction.invoke(
                        TransactionCreateAction.ShowAccountSelection(AccountSelection.FROM_ACCOUNT),
                    )
                },
                trailingContent = {
                    AccountItemDefaults.ChevronTrailing()
                },
            )
        }

        // To Account — only for transfer
        AnimatedVisibility(
            visible = state.transactionType == TransactionType.TRANSFER,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            EmSection(title = stringResource(R.string.to_account)) {
                AccountItem(
                    name = state.selectedToAccount.name,
                    icon = state.selectedToAccount.storedIcon.name,
                    iconBackgroundColor = state.selectedToAccount.storedIcon.backgroundColor,
                    amount = state.selectedToAccount.amount.amountString,
                    amountTone = state.selectedToAccount.amountTone,
                    customImagePath = state.selectedToAccount.storedIcon.customImagePath,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onAction.invoke(
                            TransactionCreateAction.ShowAccountSelection(AccountSelection.TO_ACCOUNT),
                        )
                    },
                    trailingContent = {
                        AccountItemDefaults.ChevronTrailing()
                    },
                )
            }
        }

        // Projected Closing Balance
        AnimatedVisibility(
            visible = state.projectedFromAccountBalance != null,
            enter = fadeIn(tween(200)) + expandVertically(tween(250)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(200)),
        ) {
            EmSection(title = stringResource(R.string.closing_balance)) {
                EmCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(EmTheme.spacing.md),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            EmText(
                                text = if (state.transactionType == TransactionType.TRANSFER) {
                                    stringResource(R.string.from_account) + " (${state.selectedFromAccount.name})"
                                } else {
                                    state.selectedFromAccount.name
                                },
                                role = EmTextRole.Body,
                            )
                            state.projectedFromAccountBalance?.let { balance ->
                                EmAmountText(
                                    amount = balance.amountString ?: "",
                                )
                            }
                        }
                        if (state.transactionType == TransactionType.TRANSFER && state.projectedToAccountBalance != null) {
                            Spacer(Modifier.height(EmTheme.spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                EmText(
                                    text = stringResource(R.string.to_account) + " (${state.selectedToAccount.name})",
                                    role = EmTextRole.Body,
                                )
                                EmAmountText(
                                    amount = state.projectedToAccountBalance.amountString ?: "",
                                )
                            }
                        }
                    }
                }
            }
        }

        EmSection(title = stringResource(R.string.attachments)) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = EmTheme.spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
            ) {
                items(state.attachments, key = { it }) { path ->
                    AttachmentThumbnail(
                        imagePath = path,
                        onRemove = {
                            onAction.invoke(TransactionCreateAction.RemoveAttachment(path))
                        },
                    )
                }
                item {
                    AttachmentAddTile(
                        onClick = {
                            focusManager.clearFocus(force = true)
                            onAction.invoke(TransactionCreateAction.ShowAttachmentPicker)
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }
}

/**
 * "Doesn't repeat" plus the four frequencies as chips, so the choice is one tap and always
 * visible. Once one is picked, the line below says when the next copy will be logged.
 */
@Composable
private fun RepeatSelector(
    selected: RecurringFrequency?,
    startDate: Instant,
    onSelect: (RecurringFrequency?) -> Unit,
) {
    val options = listOf<Pair<RecurringFrequency?, Int>>(
        null to R.string.repeat_never,
        RecurringFrequency.DAILY to R.string.repeat_daily,
        RecurringFrequency.WEEKLY to R.string.repeat_weekly,
        RecurringFrequency.MONTHLY to R.string.repeat_monthly,
        RecurringFrequency.YEARLY to R.string.repeat_yearly,
    )
    EmCard {
        Column(modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm)) {
                options.forEach { (frequency, label) ->
                    EmChip(
                        label = stringResource(id = label),
                        selected = selected == frequency,
                        onClick = { onSelect(frequency) },
                        leadingIcon = if (selected == frequency && frequency != null) Icons.Outlined.Repeat else null,
                    )
                }
            }
            if (selected != null) {
                // Dated on a later day: only the plan is saved, and the first entry is added then.
                val planned = remember(startDate) { isOnLaterDay(startDate) }
                val next = remember(selected, startDate) { occurrenceDate(startDate, selected, 1) }
                EmText(
                    text = if (planned) {
                        stringResource(id = R.string.repeat_planned, startDate.toCompleteDateWithDate())
                    } else {
                        stringResource(id = R.string.repeat_next, next.toCompleteDateWithDate())
                    },
                    modifier = Modifier.padding(top = EmTheme.spacing.xs),
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
        }
    }
}

fun Instant.toTime(): ReminderTimeState {
    val (hours, minutes) = hourAndMinute()
    return ReminderTimeState(
        hour = hours,
        minute = minutes,
        is24Hour = false,
    )
}

/** Same day at the picked hour and minute; seconds are kept, as before. */
fun Instant.toTime(reminderTimeState: ReminderTimeState): Instant = withTime(reminderTimeState.hour, reminderTimeState.minute, resetSeconds = false)

@EmPreviews
@Composable
private fun TransactionCreateStateForTransferPreview() {
    val amountField = TextFieldValue(value = "", valueError = false, onValueChange = {})
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        TransactionCreateScreenContent(
            state = getTransactionState(amountField, TransactionType.TRANSFER),
            onAction = {},
        )
    }
}

@EmPreviews
@Composable
private fun TransactionCreateStateForIncomePreview() {
    val amountField = TextFieldValue(value = "", valueError = false, onValueChange = {})
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        TransactionCreateScreenContent(
            state = getTransactionState(amountField, TransactionType.INCOME),
            onAction = {},
        )
    }
}

private fun getTransactionState(
    amountField: TextFieldValue<String>,
    transactionType: TransactionType,
): TransactionCreateState = TransactionCreateState(
    amount = amountField,
    currency = Currency(symbol = "$", name = ""),
    dateTime = Clock.System.now(),
    notes = amountField,
    selectedCategory = Category(
        id = "1",
        name = "Shopping",
        type = CategoryType.EXPENSE,
        StoredIcon(
            name = "account_balance_wallet",
            backgroundColor = "#000000",
        ),
        createdOn = Clock.System.now(),
        updatedOn = Clock.System.now(),
    ),
    selectedFromAccount = AccountUiModel(
        id = "1",
        name = "Shopping",
        type = AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "account_balance_wallet",
            backgroundColor = "#000000",
        ),
        amountTone = AmountTone.NEGATIVE,
        amount = Amount(0.0, "$ 0.00"),
    ),
    selectedToAccount = AccountUiModel(
        id = "1",
        name = "Shopping",
        type = AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "account_balance_wallet",
            backgroundColor = "#000000",
        ),
        amountTone = AmountTone.POSITIVE,
        amount = Amount(0.0, "$ 0.00"),
    ),
    accounts = emptyList(),
    categories = emptyList(),
    showDeleteButton = true,
    showDeleteDialog = false,
    showCategorySelection = false,
    showAccountSelection = false,
    showNumberPad = false,
    transactionType = transactionType,
    accountSelection = AccountSelection.FROM_ACCOUNT,
    showTimeSelection = false,
    showDateSelection = false,
)
