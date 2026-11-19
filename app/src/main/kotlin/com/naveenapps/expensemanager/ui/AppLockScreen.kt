package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme

@Composable
fun AppLockScreen(
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(EmTheme.colors.scheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(EmTheme.spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            EmIcon(
                icon = Icons.Outlined.Lock,
                contentDescription = null,
                size = EmTheme.icons.hero,
                tint = EmTheme.colors.scheme.primary,
            )

            Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

            EmText(
                text = stringResource(id = R.string.app_name),
                role = EmTextRole.Display,
            )

            Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

            EmText(
                text = stringResource(id = R.string.app_lock_description),
                color = EmTextColor.Supporting,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(EmTheme.spacing.xxxl))

            EmButton(
                text = stringResource(id = R.string.app_lock_unlock),
                onClick = onUnlockClick,
                leadingIcon = Icons.Outlined.Fingerprint,
            )
        }
    }
}
