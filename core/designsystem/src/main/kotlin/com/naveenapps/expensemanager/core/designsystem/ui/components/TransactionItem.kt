package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.R
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.theme.tint
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getColorValue
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TransactionItem(
    categoryName: String,
    fromAccountName: String,
    fromAccountIcon: String,
    fromAccountColor: String,
    amount: Amount,
    date: String,
    notes: String?,
    categoryColor: String,
    categoryIcon: String,
    modifier: Modifier = Modifier,
    toAccountName: String? = null,
    toAccountIcon: String? = null,
    toAccountColor: String? = null,
    transactionType: TransactionType = TransactionType.EXPENSE,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isRecurring: Boolean = false,
) {
    val isTransfer = toAccountName?.isNotBlank() == true

    val density = LocalDensity.current
    val swipeMax = with(density) { 140.dp.toPx() }
    val snapAt = with(density) { 60.dp.toPx() }
    var rawOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = rawOffset,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe",
    )
    val revealFraction = (abs(animatedOffset) / swipeMax).coerceIn(0f, 1f)
    // offset {} is mirrored in RTL (the actions sit on the left there), but drag deltas are
    // not, so flip them to keep "swipe towards the start edge" revealing the actions.
    val dragDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val editLabel = stringResource(R.string.edit)
    val deleteLabel = stringResource(R.string.delete)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(EmTheme.shapes.xs), // clip overflow from swipe
    ) {
        // ── Revealed actions (behind the row) ──────────────────────
        if (onEdit != null || onDelete != null) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(revealFraction),
                horizontalArrangement = Arrangement.End,
            ) {
                if (onEdit != null) {
                    SwipeAction(
                        icon = Icons.Outlined.Edit,
                        label = editLabel,
                        color = EmTheme.colors.scheme.primary,
                        onClick = {
                            rawOffset = 0f
                            onEdit()
                        },
                    )
                }
                if (onDelete != null) {
                    SwipeAction(
                        icon = Icons.Outlined.Delete,
                        label = deleteLabel,
                        color = EmTheme.colors.expense,
                        onClick = {
                            rawOffset = 0f
                            onDelete()
                        },
                    )
                }
            }
        }

        // ── Foreground row ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(EmTheme.colors.scheme.surfaceContainerLowest)
                .then(
                    if (onEdit != null || onDelete != null) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    rawOffset = if (rawOffset < -snapAt) -swipeMax else 0f
                                },
                                onHorizontalDrag = { _, delta ->
                                    rawOffset = (rawOffset + delta * dragDirection).coerceIn(-swipeMax, 0f)
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                // Swipe-only actions are unreachable with TalkBack; expose them as actions.
                .semantics {
                    customActions = listOfNotNull(
                        onEdit?.let { edit ->
                            CustomAccessibilityAction(editLabel) {
                                edit()
                                true
                            }
                        },
                        onDelete?.let { delete ->
                            CustomAccessibilityAction(deleteLabel) {
                                delete()
                                true
                            }
                        },
                    )
                }
                .then(
                    if (onClick != null) {
                        // Default ripple, so a tap gives visible feedback.
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md)
                .animateContentSize(),
        ) {
            // ── Category / transfer icon ───────────────────────────
            CategoryIconBox(
                icon = if (isTransfer) "ic_transfer_account" else categoryIcon,
                iconColor = if (isTransfer) {
                    String.format("#%08X", EmTheme.colors.scheme.tertiary.toArgb())
                } else {
                    categoryColor
                },
                contentDescription = categoryName,
                modifier = Modifier.align(Alignment.CenterVertically),
            )

            Spacer(Modifier.width(EmTheme.spacing.md))

            // ── Centre text block ──────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
            ) {
                if (isTransfer && toAccountIcon != null && toAccountColor != null) {
                    TransferAccountsRow(
                        fromIcon = fromAccountIcon,
                        fromColor = fromAccountColor,
                        fromName = fromAccountName,
                        toIcon = toAccountIcon,
                        toColor = toAccountColor,
                        toName = toAccountName,
                    )
                } else {
                    // ── Title ──
                    EmText(
                        text = categoryName,
                        modifier = Modifier.fillMaxWidth(),
                        role = EmTextRole.ItemTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(Modifier.height(EmTheme.spacing.xs))

                    // ── Subtitle: account with dot separator ──
                    AccountSubtitle(
                        accountIcon = fromAccountIcon,
                        accountColor = fromAccountColor,
                        accountName = fromAccountName,
                    )

                    // ── Notes (if any) ──
                    if (!notes.isNullOrBlank()) {
                        Spacer(Modifier.height(EmTheme.spacing.xxs))
                        EmText(
                            text = notes,
                            modifier = Modifier.fillMaxWidth(),
                            role = EmTextRole.Supporting,
                            color = EmTextColor.Supporting,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Spacer(Modifier.width(EmTheme.spacing.md))

            // ── Amount + date ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End,
            ) {
                val (prefix, amountColor) = when (transactionType) {
                    TransactionType.EXPENSE -> "−" to EmTheme.colors.expense

                    TransactionType.INCOME -> "+" to EmTheme.colors.income

                    else -> "" to Color.Unspecified
                }

                EmAmountText(
                    amount = "$prefix${amount.amountString ?: ""}",
                    color = amountColor,
                )

                Spacer(Modifier.height(EmTheme.spacing.xxs))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Added by a recurring rule: a small repeat mark before the date.
                    if (isRecurring) {
                        EmIcon(
                            icon = Icons.Outlined.Repeat,
                            contentDescription = stringResource(R.string.recurring_badge),
                            size = EmTheme.icons.sm,
                            tint = EmTheme.colors.scheme.primary,
                        )
                        Spacer(Modifier.width(EmTheme.spacing.xs))
                    }
                    EmText(
                        text = date,
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════
//  Sub-components
// ═══════════════════════════════════════════════════════════════════════

/**
 * Rounded-rect icon container with a tinted background derived from the
 * category colour at low alpha – gives a cohesive, badge-like look.
 */
@Composable
private fun CategoryIconBox(
    icon: String,
    iconColor: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tint = Color(getColorValue(iconColor))

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(EmTheme.shapes.md)
            .background(tint.tint())
            .border(
                width = 1.dp,
                color = tint.tint(),
                shape = EmTheme.shapes.md,
            ),
        contentAlignment = Alignment.Center,
    ) {
        EmIcon(
            painter = painterResource(id = context.getDrawable(icon)),
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}

/**
 * Account name preceded by its icon, used beneath the title.
 * Includes a subtle dot separator style that pairs well with small text.
 */
@Composable
private fun AccountSubtitle(
    accountIcon: String,
    accountColor: String,
    accountName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tint = Color(getColorValue(accountColor))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        EmIcon(
            painter = painterResource(id = context.getDrawable(accountIcon)),
            contentDescription = null,
            size = EmTheme.icons.sm,
            tint = tint,
        )
        Spacer(Modifier.width(EmTheme.spacing.xs))
        EmText(
            text = accountName,
            role = EmTextRole.Supporting,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Two-line transfer layout: "From → To" with an arrow between accounts.
 */
@Composable
private fun TransferAccountsRow(
    fromIcon: String,
    fromColor: String,
    fromName: String,
    toIcon: String,
    toColor: String,
    toName: String,
) {
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(EmTheme.spacing.xxs)) {
        // From account
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmIcon(
                painter = painterResource(id = context.getDrawable(fromIcon)),
                contentDescription = null,
                size = EmTheme.icons.sm,
                tint = Color(getColorValue(fromColor)),
            )
            Spacer(Modifier.width(EmTheme.spacing.xs))
            EmText(
                text = fromName,
                role = EmTextRole.Supporting,
                color = Color(getColorValue(fromColor)).copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Arrow
        EmText(
            text = "↓",
            modifier = Modifier.padding(start = EmTheme.spacing.xs),
            role = EmTextRole.Supporting,
            color = EmTextColor.Supporting,
        )

        // To account
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmIcon(
                painter = painterResource(id = context.getDrawable(toIcon)),
                contentDescription = null,
                size = EmTheme.icons.sm,
                tint = Color(getColorValue(toColor)),
            )
            Spacer(Modifier.width(EmTheme.spacing.xs))
            EmText(
                text = toName,
                role = EmTextRole.Supporting,
                color = Color(getColorValue(toColor)).copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Individual swipe-revealed action button (Edit / Delete).
 */
@Composable
private fun SwipeAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(70.dp)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EmIcon(
                icon = icon,
                contentDescription = label,
                size = EmTheme.icons.md,
                tint = Color.White,
            )
            Spacer(Modifier.height(EmTheme.spacing.xs))
            EmText(
                text = label,
                role = EmTextRole.Label,
                color = Color.White,
            )
        }
    }
}

@EmPreviews
@Composable
fun TransactionUiStatePreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        EmCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EmTheme.spacing.lg),
        ) {
            TransactionItem(
                categoryName = "Utilities",
                categoryIcon = "agriculture",
                categoryColor = "#A65A56",
                fromAccountName = "Card-xxx",
                fromAccountIcon = "account_balance",
                fromAccountColor = "#A65A56",
                amount = Amount(amount = 300.0, amountString = "300 ₹"),
                date = "15/11/2019",
                notes = "Sample notes given as per transaction",
            )
        }
    }
}
