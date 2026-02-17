package com.naveenapps.expensemanager.feature.transaction.list.new

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt

// ── Typography helpers ─────────────────────────────────────────────────
// Replace with your actual font resources; these act as placeholders.
// val DmSans = FontFamily(Font(R.font.dm_sans_regular), Font(R.font.dm_sans_medium, FontWeight.Medium), ...)
// val SpaceMono = FontFamily(Font(R.font.space_mono_regular), Font(R.font.space_mono_bold, FontWeight.Bold))
// For now we fall back to Default:
val DmSans   = FontFamily.Default
val SpaceMono = FontFamily.Monospace

// ── Summary Card ───────────────────────────────────────────────────────

@Composable
fun SummaryCard(
    label: String,
    amount: Double,
    accentColor: Color,
    gradientStart: Color,
    modifier: Modifier = Modifier,
) {
    val formattedAmount = "$${"%,.2f".format(amount)}"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(listOf(gradientStart, AppColors.Surface))
            )
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DmSans,
                    color = accentColor.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formattedAmount,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceMono,
                color = accentColor,
                letterSpacing = (-0.3).sp,
            )
        }
    }
}

// ── Account Filter Pill ────────────────────────────────────────────────

@Composable
fun AccountPill(
    icon: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        if (isSelected) AppColors.Accent else AppColors.CardBorder,
        animationSpec = tween(200),
        label = "pill_border",
    )
    val bgColor by animateColorAsState(
        if (isSelected) AppColors.Accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.02f),
        animationSpec = tween(200),
        label = "pill_bg",
    )
    val textColor by animateColorAsState(
        if (isSelected) AppColors.Accent else AppColors.TextSecondary,
        animationSpec = tween(200),
        label = "pill_text",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = DmSans,
                color = textColor,
            )
        }
    }
}

// ── Type-filter tab bar ────────────────────────────────────────────────

@Composable
fun TypeFilterBar(
    selected: TypeFilter,
    counts: Map<TypeFilter, Int>,
    onSelect: (TypeFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Card)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TypeFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            val bg by animateColorAsState(
                if (isSelected) AppColors.ActiveTab else Color.Transparent,
                animationSpec = tween(250),
                label = "tab_bg",
            )
            val textCol by animateColorAsState(
                if (isSelected) AppColors.TextPrimary else AppColors.TextTertiary,
                animationSpec = tween(250),
                label = "tab_text",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(filter) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = filter.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontFamily = DmSans,
                        color = textCol,
                    )
                    if (filter != TypeFilter.ALL) {
                        Spacer(Modifier.width(6.dp))
                        val badgeBg = if (isSelected)
                            AppColors.Accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                        val badgeText = if (isSelected) AppColors.Accent else AppColors.TextTertiary
                        Text(
                            text = "${counts[filter] ?: 0}",
                            fontSize = 11.sp,
                            fontFamily = DmSans,
                            color = badgeText,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Collapsible Search Bar ─────────────────────────────────────────────

@Composable
fun CollapsibleSearchBar(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(tween(300)),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(tween(300)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.SearchBg)
                .border(1.dp, AppColors.CardBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.TextTertiary,
                    modifier = Modifier.size(18.dp),
                )
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Search transactions…",
                            fontSize = 14.sp,
                            fontFamily = DmSans,
                            color = AppColors.TextTertiary,
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = AppColors.Accent,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary,
                    ),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = DmSans,
                    ),
                    modifier = Modifier.weight(1f),
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

// ── Swipeable Transaction Row ──────────────────────────────────────────

@Composable
fun SwipeableTransactionRow(
    transaction: Transaction,
    showDivider: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val swipeThreshold = with(LocalDensity.current) { 140.dp.toPx() }
    val snapThreshold  = with(LocalDensity.current) { 60.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipe_offset",
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // ── Revealed actions behind the row ──
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 0.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            // Edit
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .background(AppColors.Accent)
                    .clickable { onEdit(); offsetX = 0f },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSans, color = Color.White)
                }
            }
            // Delete
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .background(AppColors.Expense)
                    .clickable { onDelete(); offsetX = 0f },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = DmSans, color = Color.White)
                }
            }
        }

        // ── Foreground content ──
        Column(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .background(AppColors.Card)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            offsetX = if (offsetX < -snapThreshold) -swipeThreshold else 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-swipeThreshold, 0f)
                        },
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                // ── Category icon ──
                val cat = transaction.category
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cat.color.copy(alpha = 0.08f))
                        .border(1.dp, cat.color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = cat.icon, fontSize = 20.sp)
                }

                Spacer(Modifier.width(14.dp))

                // ── Title + subtitle ──
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = DmSans,
                        color = AppColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cat.label,
                            fontSize = 11.sp,
                            fontFamily = DmSans,
                            color = AppColors.TextTertiary,
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(AppColors.TextQuaternary)
                        )
                        Spacer(Modifier.width(6.dp))
                        val acc = transaction.account
                        Text(
                            text = "${acc.icon} ${acc.name}",
                            fontSize = 11.sp,
                            fontFamily = DmSans,
                            color = AppColors.TextQuaternary,
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // ── Amount + time ──
                Column(horizontalAlignment = Alignment.End) {
                    val sign = if (transaction.type == TransactionType.INCOME) "+" else "−"
                    val color = if (transaction.type == TransactionType.INCOME) AppColors.Income else AppColors.TextPrimary
                    Text(
                        text = "$sign$${"%,.2f".format(abs(transaction.amount))}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SpaceMono,
                        color = color,
                        letterSpacing = (-0.3).sp,
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = transaction.time,
                        fontSize = 11.sp,
                        fontFamily = DmSans,
                        color = AppColors.TextQuaternary,
                    )
                }
            }

            if (showDivider) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 1.dp, color = AppColors.Divider)
            }
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No transactions found",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = DmSans,
            color = AppColors.TextSecondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Try adjusting your filters or search",
            fontSize = 13.sp,
            fontFamily = DmSans,
            color = AppColors.TextTertiary,
        )
    }
}
