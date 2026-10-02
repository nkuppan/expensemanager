package com.naveenapps.expensemanager.feature.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.ReminderTimeState
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionBottomSheet
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import org.koin.compose.viewmodel.koinViewModel

// Matches the literal %1$s placeholder left in privacy_text when it's read via stringResource()
private const val LINK_PLACEHOLDER = "%1\$s"

/**
 * The app's single first-run screen (it replaced the separate Intro + Setup screens).
 * Everything is pre-filled — currency from the device locale, accounts preloaded — so the user
 * can go straight to logging their first expense with one tap.
 */
@Composable
fun OnboardingScreen(
    shareRepository: ShareRepository? = null,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    OnboardingContentView(
        state = state,
        shareRepository = shareRepository,
        onAction = viewModel::processAction,
    )
}

@Composable
private fun OnboardingContentView(
    state: OnboardingState,
    shareRepository: ShareRepository?,
    onAction: (OnboardingAction) -> Unit,
) {
    if (state.showCurrencySelection) {
        CountryCurrencySelectionBottomSheet(
            onEvent = { event ->
                when (event) {
                    CountrySelectionEvent.Dismiss -> {
                        onAction.invoke(OnboardingAction.DismissCurrencySelection)
                    }

                    is CountrySelectionEvent.CountrySelected -> {
                        onAction.invoke(OnboardingAction.SelectCurrency(event.country))
                    }
                }
            },
        )
    }

    // Both buttons finish setup. If the reminder switch is on and Android 13+ still needs the
    // notification permission, ask now (the reminder card explains why), then continue with
    // whichever button was tapped, whatever the answer.
    val context = LocalContext.current
    var pendingFinish by remember { mutableStateOf<OnboardingAction?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onAction.invoke(OnboardingAction.NotificationPermissionResult(granted))
        pendingFinish?.let(onAction)
        pendingFinish = null
    }
    val finish: (OnboardingAction) -> Unit = { action ->
        val needsPermission = state.reminderEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            pendingFinish = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onAction.invoke(action)
        }
    }

    Scaffold(
        bottomBar = {
            OnboardingBottomBar(
                onAddFirstExpense = { finish(OnboardingAction.AddFirstExpense) },
                onExplore = { finish(OnboardingAction.Next) },
                shareRepository = shareRepository,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            WelcomeHeader()

            Spacer(modifier = Modifier.height(24.dp))

            StepSection(title = stringResource(id = R.string.select_main_currency)) {
                CurrencyCard(
                    currency = state.currency,
                    onClick = { onAction.invoke(OnboardingAction.ShowCurrencySelection) },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            StepSection(
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
            ) {
                AccountsSummaryCard(
                    accounts = state.accounts,
                    onClick = { onAction.invoke(OnboardingAction.OpenAccounts) },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            StepSection(title = stringResource(id = R.string.onboarding_reminder_title)) {
                ReminderCard(
                    enabled = state.reminderEnabled,
                    time = state.reminderTime,
                    onToggle = { onAction.invoke(OnboardingAction.SetReminderEnabled(it)) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WelcomeHeader() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.expense_1),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center,
        ) {
            LottieAnimation(
                composition = composition,
                reverseOnRepeat = true,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.welcome_message_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.3).sp,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.welcome_message_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrencyCard(
    currency: Currency,
    onClick: () -> Unit,
) {
    SetupRowCard(
        icon = Icons.Outlined.Payments,
        title = currency.name.ifBlank { stringResource(id = R.string.setup_currency) },
        subtitle = stringResource(id = R.string.tap_to_change),
        trailing = currency.toDisplayValue(),
        onClick = onClick,
    )
}

@Composable
private fun AccountsSummaryCard(
    accounts: List<AccountUiModel>,
    onClick: () -> Unit,
) {
    SetupRowCard(
        icon = Icons.Outlined.AccountBalanceWallet,
        title = accounts.joinToString(", ") { it.name }
            .ifBlank { stringResource(id = R.string.create_new) },
        subtitle = stringResource(id = R.string.tap_to_change),
        trailing = accounts.size.takeIf { it > 0 }?.toString().orEmpty(),
        onClick = onClick,
    )
}

/** One tappable settings row: tinted icon, title + hint, value and chevron. */
@Composable
private fun SetupRowCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: String,
    onClick: () -> Unit,
) {
    AppCardView(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (trailing.isNotBlank()) {
                    Text(
                        text = trailing,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    enabled: Boolean,
    time: ReminderTimeState?,
    onToggle: (Boolean) -> Unit,
) {
    AppCardView(
        onClick = { onToggle(!enabled) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        id = R.string.onboarding_reminder_time,
                        time?.toDisplayTime().orEmpty(),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(id = R.string.onboarding_reminder_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

/** Locale-aware short time, e.g. "10:00 AM" or "10:00". */
private fun ReminderTimeState.toDisplayTime(): String = LocalTime.of(hour, minute).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

@Composable
private fun OnboardingBottomBar(
    onAddFirstExpense: () -> Unit,
    onExplore: () -> Unit,
    shareRepository: ShareRepository?,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 0.dp,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onAddFirstExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.onboarding_add_first_expense),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Outlined (not a bare text button) so it reads as a real second choice.
                OutlinedButton(
                    onClick = onExplore,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        text = stringResource(id = R.string.onboarding_explore_first),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                PrivacyText(shareRepository = shareRepository)
            }
        }
    }
}

@Composable
private fun PrivacyText(shareRepository: ShareRepository?) {
    // privacy_text carries a %1$s placeholder for the "privacy policy" phrase rather than an
    // <annotation> span: annotations proved unreliable after an in-app locale switch.
    val template = stringResource(id = R.string.privacy_text)
    val linkText = stringResource(id = R.string.privacy_policy_link)
    val linkColor = MaterialTheme.colorScheme.primary
    Text(
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        text = buildAnnotatedString {
            val placeholderIndex = template.indexOf(LINK_PLACEHOLDER)
            if (placeholderIndex >= 0) {
                append(template.substring(0, placeholderIndex))
                val start = length
                append(linkText)
                val end = length
                append(template.substring(placeholderIndex + LINK_PLACEHOLDER.length))
                addLink(
                    url = LinkAnnotation.Url(
                        url = "",
                        linkInteractionListener = { shareRepository?.openPrivacy() },
                        styles = TextLinkStyles(
                            style = SpanStyle(color = linkColor, fontWeight = FontWeight.Medium),
                        ),
                    ),
                    start = start,
                    end = end,
                )
            } else {
                append(template)
            }
        },
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun StepSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        content()
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun OnboardingScreenPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        OnboardingContentView(
            state = OnboardingState(
                currency = Currency("$", "US Dollars", code = "USD"),
                accounts = listOf("Cash", "Card", "Bank").mapIndexed { index, name ->
                    AccountUiModel(
                        id = index.toString(),
                        name = name,
                        type = AccountType.REGULAR,
                        storedIcon = StoredIcon(
                            name = "account_balance",
                            backgroundColor = "#000000",
                        ),
                        amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
                        amount = Amount(0.0, "$ 0.00"),
                    )
                },
                showCurrencySelection = false,
            ),
            shareRepository = null,
            onAction = {},
        )
    }
}
