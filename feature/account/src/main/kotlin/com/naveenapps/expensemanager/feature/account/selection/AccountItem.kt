package com.naveenapps.expensemanager.feature.account.selection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.naveenapps.expensemanager.core.designsystem.em.EmAmountText
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmCardPosition
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.em.emColorOf
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.feature.account.R
import com.naveenapps.expensemanager.feature.account.list.getRandomAccountUiModel

@Composable
fun AccountItem(
    name: String,
    icon: String,
    iconBackgroundColor: String,
    modifier: Modifier = Modifier,
    position: EmCardPosition = EmCardPosition.Single,
    selected: Boolean = false,
    subtitle: String? = null,
    amount: String? = null,
    amountTone: AmountTone? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    customImagePath: String? = null,
) {
    EmCard(
        position = position,
        selected = selected,
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
                    },
                )
                .padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconAndBackgroundView(
                icon = icon,
                iconBackgroundColor = iconBackgroundColor,
                name = name,
                customImagePath = customImagePath,
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = EmTheme.spacing.lg),
            ) {
                EmText(
                    text = name,
                    role = EmTextRole.ItemTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(EmTheme.spacing.xxs))
                    EmText(
                        text = subtitle,
                        role = EmTextRole.Supporting,
                        color = EmTextColor.Supporting,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (amount != null && amountTone != null) {
                EmAmountText(
                    amount = amount,
                    color = emColorOf(amountTone),
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@EmPreviews
@Composable
fun AccountItemPreview() {
    val account = getRandomAccountUiModel(0).first()

    ExpenseManagerPreviewTheme {
        AccountItem(
            name = account.name,
            icon = account.storedIcon.name,
            iconBackgroundColor = account.storedIcon.backgroundColor,
            amount = account.amount.amountString,
            amountTone = account.amountTone,
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
            amountTone = account.amountTone,
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
