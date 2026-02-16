package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.R

/**
 * Displays payment status overview showing paid and due amounts side by side.
 *
 * @param modifier Modifier to be applied to the container
 * @param paidAmount Formatted string of the paid amount (e.g., "$1,234.56")
 * @param dueAmount Formatted string of the due amount (e.g., "$567.89")
 */
@Composable
fun OverallStatusOverview(
    expenseAmount: String,
    incomeAmount: String,
    balanceAmount: String,
    modifier: Modifier = Modifier,
    showBalance: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PaymentStatusCard(
            amount = incomeAmount,
            label = stringResource(id = R.string.income),
            statusType = PaymentStatusType.INCOME,
            modifier = Modifier.weight(1f),
        )
        PaymentStatusCard(
            amount = expenseAmount,
            label = stringResource(id = R.string.expense),
            statusType = PaymentStatusType.EXPENSE,
            modifier = Modifier.weight(1f),
        )
        if (showBalance) {
            PaymentStatusCard(
                amount = balanceAmount,
                label = stringResource(id = R.string.balance),
                modifier = Modifier.weight(1f),
                statusType = PaymentStatusType.BALANCE,
            )
        }
    }
}

/**
 * Represents the visual style for different payment statuses.
 */
enum class PaymentStatusType(
    val backgroundColor: Color,
    val contentColor: Color,
) {
    INCOME(
        backgroundColor = Color(0xFFE8F5E9), // Light emerald green
        contentColor = Color(0xFF1B5E20)     // Dark green
    ),
    EXPENSE(
        backgroundColor = Color(0xFFFFEBEE), // Light red
        contentColor = Color(0xFFB71C1C)     // Dark red
    ),
    BALANCE(
        backgroundColor = Color(0xFFE8F5E9), // Light Grey
        contentColor = Color(0xFF1B5E20)     // Dark Grey
    )
}

/**
 * A card component that displays a payment amount with colored styling.
 *
 * @param amount The monetary amount to display (pre-formatted)
 * @param label The descriptive label (e.g., "PAID", "DUE")
 * @param statusType The visual style to apply based on payment status
 * @param modifier Modifier to be applied to the card
 */
@Composable
fun PaymentStatusCard(
    amount: String,
    label: String,
    statusType: PaymentStatusType,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = statusType.backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Label text (e.g., "PAID", "DUE")
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = statusType.contentColor.copy(alpha = 0.7f)
            )

            // Amount text
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                ),
                color = statusType.contentColor,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

const val AMOUNT_VALUE = "₹100000.00"

@AppPreviewsLightAndDarkMode
@Composable
private fun AmountStatusViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        OverallStatusOverview(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            incomeAmount = AMOUNT_VALUE,
            expenseAmount = AMOUNT_VALUE,
            balanceAmount = AMOUNT_VALUE,
        )
        OverallStatusOverview(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            incomeAmount = AMOUNT_VALUE,
            expenseAmount = AMOUNT_VALUE,
            balanceAmount = AMOUNT_VALUE,
        )
    }
}
