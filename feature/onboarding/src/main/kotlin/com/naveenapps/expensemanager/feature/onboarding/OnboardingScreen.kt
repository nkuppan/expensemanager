package com.naveenapps.expensemanager.feature.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.naveenapps.expensemanager.core.designsystem.em.EmButton
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIcon
import com.naveenapps.expensemanager.core.designsystem.em.EmIconTile
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTextColor
import com.naveenapps.expensemanager.core.designsystem.em.EmTextRole
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.AmountTone
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.toDisplayValue
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.country.CountryCurrencySelectionBottomSheet
import com.naveenapps.expensemanager.feature.country.CountrySelectionEvent
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
    val state by viewModel.state.collectAsStateWithLifecycle()

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

    EmScaffold(
        bottomBar = {
            OnboardingBottomBar(
                onGetStarted = { onAction.invoke(OnboardingAction.GetStarted) },
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

            Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

            StepSection(title = stringResource(id = R.string.select_main_currency)) {
                CurrencyCard(
                    currency = state.currency,
                    onClick = { onAction.invoke(OnboardingAction.ShowCurrencySelection) },
                )
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.xl))

            StepSection(
                title = stringResource(id = com.naveenapps.expensemanager.feature.account.R.string.accounts),
            ) {
                AccountsSummaryCard(
                    accounts = state.accounts,
                    onClick = { onAction.invoke(OnboardingAction.OpenAccounts) },
                )
            }

            Spacer(modifier = Modifier.height(EmTheme.spacing.xl))
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
            .padding(horizontal = EmTheme.spacing.xl),
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

        Spacer(modifier = Modifier.height(EmTheme.spacing.lg))

        EmText(
            text = stringResource(id = R.string.welcome_message_title),
            role = EmTextRole.Display,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(EmTheme.spacing.sm))

        EmText(
            text = stringResource(id = R.string.welcome_message_description),
            color = EmTextColor.Supporting,
            textAlign = TextAlign.Center,
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
    EmCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = EmTheme.spacing.lg, vertical = EmTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmIconTile(icon = icon)
            Spacer(modifier = Modifier.width(EmTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                EmText(
                    text = title,
                    role = EmTextRole.ItemTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                EmText(
                    text = subtitle,
                    role = EmTextRole.Supporting,
                    color = EmTextColor.Supporting,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(EmTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (trailing.isNotBlank()) {
                    EmText(
                        text = trailing,
                        role = EmTextRole.Label,
                        color = EmTextColor.Primary,
                        maxLines = 1,
                    )
                }
                EmIcon(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    size = EmTheme.icons.md,
                    tint = EmTheme.colors.scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun OnboardingBottomBar(
    onGetStarted: () -> Unit,
    shareRepository: ShareRepository?,
) {
    Box(modifier = Modifier.background(EmTheme.colors.scheme.surfaceContainerLowest)) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            EmDivider()
            Column(
                modifier = Modifier.padding(
                    start = EmTheme.spacing.xl,
                    end = EmTheme.spacing.xl,
                    top = EmTheme.spacing.lg,
                    bottom = EmTheme.spacing.sm,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmButton(
                    text = stringResource(id = R.string.onboarding_get_started),
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                )
                Spacer(modifier = Modifier.height(EmTheme.spacing.xs))
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
    val linkColor = EmTheme.colors.scheme.primary
    val linkWeight = EmTheme.type.label.fontWeight
    EmText(
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
                            style = SpanStyle(color = linkColor, fontWeight = linkWeight),
                        ),
                    ),
                    start = start,
                    end = end,
                )
            } else {
                append(template)
            }
        },
        modifier = Modifier.padding(top = EmTheme.spacing.xs, bottom = EmTheme.spacing.sm),
        role = EmTextRole.Supporting,
        color = EmTextColor.Supporting,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun StepSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = EmTheme.spacing.lg),
    ) {
        EmText(
            text = title,
            modifier = Modifier.padding(bottom = EmTheme.spacing.sm),
            role = EmTextRole.SectionTitle,
        )
        content()
    }
}

@EmPreviews
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
                        amountTone = AmountTone.POSITIVE,
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
