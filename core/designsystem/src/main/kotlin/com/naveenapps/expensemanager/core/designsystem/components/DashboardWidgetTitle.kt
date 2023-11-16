package com.naveenapps.expensemanager.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.naveenapps.expensemanager.core.designsystem.R


@Composable
fun DashboardWidgetTitle(
    title: String,
    modifier: Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        Text(
            text = title, style = MaterialTheme.typography.headlineSmall
        )
        if (onViewAllClick != null) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable { onViewAllClick.invoke() },
                text = stringResource(id = R.string.view_all).uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}