package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel

@Composable
fun AccountItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    subtitle: String? = null,
    amount: String? = null,
    amountTextColor: Int? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    AppCardView(
        shape = shape,
        border = border,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick.invoke() }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (amount != null && amountTextColor != null) {
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorResource(id = amountTextColor),
                    maxLines = 1,
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun AccountItemPreview() {

    val account = getRandomAccountUiModel(0).first()

    NaveenAppsPreviewTheme {
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            amount = account.amount.amountString,
            amountTextColor = account.amountTextColor,
            onClick = { },
            trailingContent = { AccountItemDefaults.ChevronTrailing() },
        )

// Account with credit limit subtitle
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            subtitle = if (account.availableCreditLimit?.amountString?.isNotBlank() == true) {
                stringResource(R.string.available_limit)
            } else {
                null
            },
            amount = account.amount.amountString,
            amountTextColor = account.amountTextColor,
            onClick = { },
        )

// Selection screen — no amount, just checkmark
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            onClick = { },
            trailingContent = { AccountItemDefaults.SingleCheckedTrailing(true) },
        )
    }
}