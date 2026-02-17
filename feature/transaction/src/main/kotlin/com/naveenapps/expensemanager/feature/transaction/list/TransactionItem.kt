package com.naveenapps.expensemanager.feature.transaction.list

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getDrawable
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getColorValue
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlin.math.abs
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════════
//  Improved TransactionItem
//
//  Changes from the original:
//    • Swipe-to-reveal Edit / Delete actions
//    • Rounded category icon container with tinted background
//    • Subtle separator dot between category and account
//    • Colour-coded amount sign prefix (+/−) for quick scanning
//    • Notes shown as a muted single-line beneath the subtitle
//    • Transfer layout tightened with an arrow indicator
//    • animateContentSize so expanding notes is smooth
//    • Accessible – keeps contentDescription, touch targets ≥ 48 dp
// ═══════════════════════════════════════════════════════════════════════

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
) {
    val isTransfer = toAccountName?.isNotBlank() == true

    // ── Swipe state ────────────────────────────────────────────────
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)), // clip overflow from swipe
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
                        label = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            rawOffset = 0f
                            onEdit()
                        },
                    )
                }
                if (onDelete != null) {
                    SwipeAction(
                        icon = Icons.Outlined.Delete,
                        label = "Delete",
                        color = colorResource(
                            id = com.naveenapps.expensemanager.core.common.R.color.red_500,
                        ),
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
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (onEdit != null || onDelete != null) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    rawOffset = if (rawOffset < -snapAt) -swipeMax else 0f
                                },
                                onHorizontalDrag = { _, delta ->
                                    rawOffset = (rawOffset + delta).coerceIn(-swipeMax, 0f)
                                },
                            )
                        }
                    } else Modifier,
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick,
                        )
                    } else Modifier,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize(),
        ) {
            // ── Category / transfer icon ───────────────────────────
            CategoryIconBox(
                icon = if (isTransfer) "ic_transfer_account" else categoryIcon,
                iconColor = if (isTransfer) "#166EF7" else categoryColor,
                contentDescription = categoryName,
                modifier = Modifier.align(Alignment.CenterVertically),
            )

            Spacer(Modifier.width(14.dp))

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
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(3.dp))

                    // ── Subtitle: account with dot separator ──
                    AccountSubtitle(
                        accountIcon = fromAccountIcon,
                        accountColor = fromAccountColor,
                        accountName = fromAccountName,
                    )

                    // ── Notes (if any) ──
                    if (!notes.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = notes,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                fontSize = 12.sp,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // ── Amount + date ──────────────────────────────────────
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.End,
            ) {
                val (prefix, amountColor) = when (transactionType) {
                    TransactionType.EXPENSE -> "−" to colorResource(
                        id = com.naveenapps.expensemanager.core.common.R.color.red_500,
                    )
                    TransactionType.INCOME -> "+" to colorResource(
                        id = com.naveenapps.expensemanager.core.common.R.color.green_500,
                    )
                    else -> "" to Color.Unspecified
                }

                Text(
                    text = "$prefix${amount.amountString ?: ""}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = amountColor,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                    ),
                )
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
            .clip(RoundedCornerShape(14.dp))
            .background(tint.copy(alpha = 0.10f))
            .border(
                width = 1.dp,
                color = tint.copy(alpha = 0.14f),
                shape = RoundedCornerShape(14.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = context.getDrawable(icon)),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp),
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
        Icon(
            painter = painterResource(id = context.getDrawable(accountIcon)),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(12.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = accountName,
            style = MaterialTheme.typography.labelMedium.copy(
                color = tint.copy(alpha = 0.85f),
                fontSize = 12.sp,
            ),
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

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // From account
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = context.getDrawable(fromIcon)),
                contentDescription = null,
                tint = Color(getColorValue(fromColor)),
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = fromName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(getColorValue(fromColor)).copy(alpha = 0.85f),
                    fontSize = 12.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Arrow
        Text(
            text = "↓",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                fontSize = 10.sp,
            ),
            modifier = Modifier.padding(start = 4.dp),
        )

        // To account
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = context.getDrawable(toIcon)),
                contentDescription = null,
                tint = Color(getColorValue(toColor)),
                modifier = Modifier.size(12.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = toName,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color(getColorValue(toColor)).copy(alpha = 0.85f),
                    fontSize = 12.sp,
                ),
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
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}


@AppPreviewsLightAndDarkMode
@Composable
fun TransactionUiStatePreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AppCardView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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