package com.naveenapps.expensemanager.feature.transaction.import.review

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppTimePickerDialog
import com.naveenapps.expensemanager.core.designsystem.ui.components.ClickableTextField
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.feature.account.selection.AccountItem
import com.naveenapps.expensemanager.feature.account.selection.AccountItemDefaults
import com.naveenapps.expensemanager.feature.account.selection.AccountSelectionScreen
import com.naveenapps.expensemanager.feature.category.selection.CategorySelectionScreen
import com.naveenapps.expensemanager.feature.transaction.R
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ImportCardStack(
    state: ImportState,
    onAction: (ImportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val draft = state.currentDraft
    val isCompact = LocalConfiguration.current.screenWidthDp <= 480
    if (draft == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(
                    R.string.import_all_reviewed,
                    state.acceptedCount,
                    state.rejectedCount,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    var showSwipeHint by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(state.currentIndex) {
        if (state.currentIndex > 0) {
            showSwipeHint = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp),
    ) {
        if (state.pendingDrafts.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(horizontal = 24.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    ),
            )
        }

        if (showSwipeHint) {
            Text(
                text = stringResource(R.string.import_swipe_hint),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        SwipeableImportCard(
            draft = draft,
            state = state,
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (draft.parsed.isSuccess) {
                OutlinedButton(
                    onClick = { onAction(ImportAction.RejectCurrent) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.reject))
                }
                Button(
                    onClick = { onAction(ImportAction.AcceptCurrent) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.add))
                }
            } else {
                OutlinedButton(
                    onClick = { onAction(ImportAction.RejectCurrent) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = stringResource(R.string.import_skip_transaction))
                }
            }

            if (isCompact) {
                TextButton(
                    onClick = { onAction(ImportAction.SwitchToList) },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.import_review_list),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableImportCard(
    draft: ImportDraft,
    state: ImportState,
    onAction: (ImportAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val canAccept = draft.parsed.isSuccess
    val addAccessibilityLabel = stringResource(R.string.add)
    val skipAccessibilityLabel = stringResource(R.string.import_skip_transaction)
    val density = LocalDensity.current
    val maxOffset = with(density) { 180.dp.toPx() }
    val swipeThreshold = with(density) { 128.dp.toPx() }
    val velocityThreshold = with(density) { 1400.dp.toPx() }
    val minimumVelocityDistance = with(density) { 32.dp.toPx() }
    val offsetX = remember(draft.parsed.id, state.currentIndex) { Animatable(0f) }
    var dragOffset by remember(draft.parsed.id, state.currentIndex) { mutableFloatStateOf(0f) }
    var isDragging by remember(draft.parsed.id, state.currentIndex) { mutableStateOf(false) }
    val dragScope = rememberCoroutineScope()
    val renderedOffset = if (isDragging) dragOffset else offsetX.value
    val swipeFraction = (renderedOffset / swipeThreshold).coerceIn(-1f, 1f)
    val swipeBackgroundColor = when {
        swipeFraction > 0.1f -> MaterialTheme.colorScheme.primary
        swipeFraction < -0.1f -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }
    val swipeContentColor = when {
        swipeFraction > 0.1f -> MaterialTheme.colorScheme.onPrimary
        swipeFraction < -0.1f -> MaterialTheme.colorScheme.onError
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .semantics {
                customActions = listOfNotNull(
                    if (canAccept) {
                        CustomAccessibilityAction(addAccessibilityLabel) {
                            onAction(ImportAction.AcceptCurrent)
                            true
                        }
                    } else {
                        null
                    },
                    CustomAccessibilityAction(skipAccessibilityLabel) {
                        onAction(ImportAction.RejectCurrent)
                        true
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(abs(swipeFraction).coerceIn(0f, 1f))
                .background(
                    swipeBackgroundColor.copy(alpha = if (swipeBackgroundColor == Color.Transparent) 0f else 0.85f),
                    RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (swipeFraction > 0 && canAccept) {
                    stringResource(R.string.add)
                } else if (swipeFraction != 0f && (swipeFraction < 0 || !canAccept)) {
                    stringResource(R.string.import_skip_transaction)
                } else {
                    ""
                },
                style = MaterialTheme.typography.headlineMedium,
                color = swipeContentColor,
            )
        }

        AppCardView(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .offset { IntOffset(renderedOffset.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        dragOffset = (dragOffset + delta).coerceIn(-maxOffset, maxOffset)
                    },
                    onDragStarted = {
                        dragOffset = 0f
                        isDragging = true
                        dragScope.launch { offsetX.stop() }
                    },
                    onDragStopped = { velocity ->
                        isDragging = false
                        offsetX.snapTo(dragOffset)
                        val direction = when {
                            abs(velocity) >= velocityThreshold &&
                                abs(dragOffset) >= minimumVelocityDistance -> {
                                if (velocity > 0f) 1f else -1f
                            }
                            dragOffset > swipeThreshold -> 1f
                            dragOffset < -swipeThreshold -> -1f
                            else -> 0f
                        }
                        val target = if (direction > 0f) maxOffset else -maxOffset
                        if (direction == 0f) {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            )
                            dragOffset = 0f
                        } else {
                            offsetX.animateTo(
                                targetValue = target,
                                animationSpec = tween(durationMillis = 180),
                                initialVelocity = velocity,
                            )
                            dragOffset = target
                            onAction(
                                if (direction > 0f && canAccept) {
                                    ImportAction.AcceptCurrent
                                } else {
                                    ImportAction.RejectCurrent
                                },
                            )
                        }
                    },
                ),
        ) {
            EditableImportCardContent(
                draft = draft,
                state = state,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun EditableImportCardContent(
    draft: ImportDraft,
    state: ImportState,
    onAction: (ImportAction) -> Unit,
) {
    val scroll = rememberScrollState()
    var showTransactionDetails by rememberSaveable(draft.parsed.id) { mutableStateOf(true) }

    if (draft.showDateSelection) {
        AppDatePickerDialog(
            selectedDate = draft.dateTime,
            onDateSelected = { picked ->
                val merged = mergeDateAndTime(picked, draft.dateTime)
                onAction(ImportAction.UpdateDateTime(draft.parsed.id, merged))
                onAction(ImportAction.DismissSelectors(draft.parsed.id))
            },
            onDismiss = { onAction(ImportAction.DismissSelectors(draft.parsed.id)) },
        )
    }
    if (draft.showTimeSelection) {
        val cal = Calendar.getInstance().apply { time = draft.dateTime }
        AppTimePickerDialog(
            reminderTimeState = Triple(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false,
            ),
            onTimeSelected = { triple ->
                val updated = mergeTime(draft.dateTime, triple.first, triple.second)
                onAction(ImportAction.UpdateDateTime(draft.parsed.id, updated))
                onAction(ImportAction.DismissSelectors(draft.parsed.id))
            },
            onDismiss = { onAction(ImportAction.DismissSelectors(draft.parsed.id)) },
        )
    }
    if (draft.showAccountSelection) {
        SafeModalBottomSheet(
            onDismissRequest = { onAction(ImportAction.DismissSelectors(draft.parsed.id)) },
        ) {
            AccountSelectionScreen(
                accounts = state.accounts,
                selectedAccount = draft.selectedAccount,
                onItemSelection = { onAction(ImportAction.SelectAccount(draft.parsed.id, it)) },
            )
        }
    }
    if (draft.showCategorySelection) {
        SafeModalBottomSheet(
            onDismissRequest = { onAction(ImportAction.DismissSelectors(draft.parsed.id)) },
        ) {
            CategorySelectionScreen(
                categories = state.categories.filter {
                    if (draft.transactionType == TransactionType.INCOME) {
                        it.type.name == "INCOME"
                    } else {
                        it.type.name == "EXPENSE"
                    }
                }.ifEmpty { state.categories },
                selectedCategory = draft.selectedCategory,
                onItemSelection = { onAction(ImportAction.SelectCategory(draft.parsed.id, it)) },
            )
        }
    }

    val isCompact = LocalConfiguration.current.screenWidthDp <= 480

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isCompact) 12.dp else 16.dp)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = draft.parsed.counterpartyName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                if (draft.isDuplicate) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.import_possible_duplicate),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.import_transaction_identity_format,
                    draft.dateTime.toCompleteDateWithDate(),
                    draft.dateTime.toTimeAndMinutes(),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        val parseError: String? = draft.parsed.parseError
        if (parseError != null && !draft.dateManuallyCorrected) {
            Text(
                text = parseError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (!draft.parsed.isSuccess) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.import_bank_failed_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = stringResource(R.string.import_bank_failed_detail),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        SettingsSection(
            title = stringResource(R.string.import_classification_section),
        ) {
            AppCardView {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    ImportTransactionTypeSelector(
                        selectedType = draft.transactionType,
                        enabled = draft.parsed.isSuccess,
                        onTypeChange = {
                            onAction(ImportAction.UpdateType(draft.parsed.id, it))
                        },
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.details)) {
            AppCardView {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedTextField(
                        value = draft.amountText,
                        onValueChange = {
                            onAction(ImportAction.UpdateAmount(draft.parsed.id, it))
                        },
                        label = { Text(stringResource(R.string.amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = draft.parsed.isSuccess,
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                        ),
                        shape = RoundedCornerShape(8.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ClickableTextField(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (draft.parsed.isSuccess) 1f else 0.6f),
                            value = draft.dateTime.toCompleteDateWithDate(),
                            label = R.string.select_date,
                            leadingIcon = Icons.Outlined.EditCalendar,
                            onClick = {
                                if (draft.parsed.isSuccess) {
                                    onAction(ImportAction.ShowDateSelection(draft.parsed.id))
                                }
                            },
                        )
                        ClickableTextField(
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (draft.parsed.isSuccess) 1f else 0.6f),
                            value = draft.dateTime.toTimeAndMinutes(),
                            label = R.string.select_time,
                            leadingIcon = Icons.Outlined.AccessTime,
                            onClick = {
                                if (draft.parsed.isSuccess) {
                                    onAction(ImportAction.ShowTimeSelection(draft.parsed.id))
                                }
                            },
                        )
                    }

                    OutlinedTextField(
                        value = draft.notes,
                        onValueChange = {
                            onAction(ImportAction.UpdateNotes(draft.parsed.id, it))
                        },
                        label = { Text(stringResource(R.string.notes)) },
                        placeholder = { Text(stringResource(R.string.optional_details)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = draft.parsed.isSuccess,
                        maxLines = if (isCompact) 2 else 3,
                        shape = RoundedCornerShape(8.dp),
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.select_category)) {
            ImportCategoryShortcuts(
                draft = draft,
                categories = if (draft.transactionType == TransactionType.INCOME) {
                    state.topIncomeCategories
                } else {
                    state.topExpenseCategories
                },
                onAction = onAction,
            )
        }

        SettingsSection(title = stringResource(R.string.select_account)) {
            AccountItem(
                name = draft.selectedAccount?.name ?: stringResource(R.string.select_account),
                icon = draft.selectedAccount?.storedIcon?.name ?: "account_balance_wallet",
                iconBackgroundColor = draft.selectedAccount?.storedIcon?.backgroundColor ?: "#DBEAFE",
                customImagePath = draft.selectedAccount?.storedIcon?.customImagePath,
                amount = draft.selectedAccount?.amount?.amountString,
                amountTextColor = draft.selectedAccount?.amountTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (draft.parsed.isSuccess) 1f else 0.6f),
                onClick = if (draft.parsed.isSuccess) {
                    { onAction(ImportAction.ShowAccountSelection(draft.parsed.id)) }
                } else {
                    null
                },
                trailingContent = {
                    AccountItemDefaults.ChevronTrailing()
                },
            )
        }

        TextButton(
            onClick = { showTransactionDetails = !showTransactionDetails },
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (showTransactionDetails) {
                    stringResource(R.string.import_hide_transaction_details)
                } else {
                    stringResource(R.string.import_transaction_details)
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }

        if (showTransactionDetails) {
            Text(
                text = stringResource(
                    R.string.import_transaction_details_format,
                    draft.parsed.counterpartyName,
                    draft.parsed.counterpartyVpa,
                    draft.parsed.bankName,
                    draft.parsed.accountNumber,
                    draft.parsed.referenceId,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ImportCategoryShortcuts(
    draft: ImportDraft,
    categories: List<Category>,
    onAction: (ImportAction) -> Unit,
) {
    val visibleCategories = categories.take(3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        visibleCategories.forEach { category ->
            FilterChip(
                selected = category.id == draft.selectedCategory?.id,
                onClick = {
                    if (category.id != draft.selectedCategory?.id) {
                        onAction(ImportAction.SelectCategory(draft.parsed.id, category))
                    }
                },
                enabled = draft.parsed.isSuccess,
                label = {
                    Text(
                        text = category.titleResId?.let { stringResource(it) }
                            ?: category.name,
                        maxLines = 1,
                    )
                },
            )
        }
        AssistChip(
            onClick = {
                onAction(ImportAction.ShowCategorySelection(draft.parsed.id))
            },
            enabled = draft.parsed.isSuccess,
            label = { Text(stringResource(R.string.more_categories)) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun ImportTransactionTypeSelector(
    selectedType: TransactionType,
    enabled: Boolean,
    onTypeChange: (TransactionType) -> Unit,
) {
    val types = listOf(
        ImportTypeOption(
            type = TransactionType.EXPENSE,
            label = R.string.expense,
            icon = Icons.AutoMirrored.Rounded.TrendingUp,
            activeContainerColor = MaterialTheme.colorScheme.errorContainer,
            activeContentColor = MaterialTheme.colorScheme.onErrorContainer,
            activeBorderColor = MaterialTheme.colorScheme.error,
        ),
        ImportTypeOption(
            type = TransactionType.INCOME,
            label = R.string.income,
            icon = Icons.AutoMirrored.Rounded.TrendingDown,
            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            activeBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
    val selectedColor = types.first { it.type == selectedType }.activeBorderColor

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, option ->
            val selected = selectedType == option.type
            SegmentedButton(
                selected = selected,
                onClick = { onTypeChange(option.type) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = option.activeContainerColor,
                    activeContentColor = option.activeContentColor,
                    activeBorderColor = option.activeBorderColor,
                ),
                border = SegmentedButtonDefaults.borderStroke(color = selectedColor),
                icon = {
                    SegmentedButtonDefaults.Icon(active = selected) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.width(18.dp),
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(option.label),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

private data class ImportTypeOption(
    val type: TransactionType,
    val label: Int,
    val icon: ImageVector,
    val activeContainerColor: Color,
    val activeContentColor: Color,
    val activeBorderColor: Color,
)

private fun mergeDateAndTime(datePart: Date, timePart: Date): Date {    val dateCal = Calendar.getInstance().apply { time = datePart }
    val timeCal = Calendar.getInstance().apply { time = timePart }
    dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
    dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
    dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND))
    return dateCal.time
}

private fun mergeTime(base: Date, hour: Int, minute: Int): Date {
    val cal = Calendar.getInstance().apply { time = base }
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    return cal.time
}
