package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet

private const val HABIT_DAYS = 7

/**
 * Shown once, right after the install's first saved transaction. It rewards the action and
 * sets up the habit: day 1 of 7 is filled in, and the main button goes straight back to the
 * keypad while the user is still motivated.
 */
@Composable
fun FirstSaveCelebrationSheet(
    onAddAnother: () -> Unit,
    onDone: () -> Unit,
) {
    SafeModalBottomSheet(onDismissRequest = onDone) {
        FirstSaveCelebrationContent(onAddAnother = onAddAnother, onDone = onDone)
    }
}

@Composable
private fun FirstSaveCelebrationContent(
    onAddAnother: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(id = R.string.first_save_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.first_save_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        HabitProgress(completedDays = 1)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.first_save_progress, 1, HABIT_DAYS),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onAddAnother,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(id = R.string.first_save_add_another),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(id = R.string.first_save_done),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** A row of [HABIT_DAYS] pills, the first [completedDays] filled in the brand green. */
@Composable
private fun HabitProgress(completedDays: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(HABIT_DAYS) { day ->
            val done = day < completedDays
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 8.dp)
                    .background(
                        color = if (done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(50),
                    ),
            )
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
private fun FirstSaveCelebrationContentPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        FirstSaveCelebrationContent(onAddAnother = {}, onDone = {})
    }
}
