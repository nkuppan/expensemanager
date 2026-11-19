package com.naveenapps.expensemanager.feature.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.common.utils.getAppVersionName
import com.naveenapps.expensemanager.core.designsystem.em.EmCard
import com.naveenapps.expensemanager.core.designsystem.em.EmDivider
import com.naveenapps.expensemanager.core.designsystem.em.EmIconButton
import com.naveenapps.expensemanager.core.designsystem.em.EmPreviews
import com.naveenapps.expensemanager.core.designsystem.em.EmScaffold
import com.naveenapps.expensemanager.core.designsystem.em.EmSettingItem
import com.naveenapps.expensemanager.core.designsystem.em.EmText
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBar
import com.naveenapps.expensemanager.core.designsystem.em.EmTopBarNavigation
import com.naveenapps.expensemanager.core.designsystem.theme.EmTheme
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerPreviewTheme
import com.naveenapps.expensemanager.core.repository.ShareRepository

@Composable
fun AboutScreen(
    shareRepository: ShareRepository,
    viewModel: AboutUsViewModel = org.koin.androidx.compose.koinViewModel(),
) {
    AboutUsScreenScaffoldView {
        when (it) {
            AboutUsOption.ABOUT_US -> {
                shareRepository.openAboutUs()
            }

            AboutUsOption.TERMS -> {
                shareRepository.openTerms()
            }

            AboutUsOption.PRIVACY_POLICY -> {
                shareRepository.openPrivacy()
            }

            AboutUsOption.GITHUB -> {
                shareRepository.openGithub()
            }

            AboutUsOption.TWITTER -> {
                shareRepository.openTwitter()
            }

            AboutUsOption.INSTAGRAM -> {
                shareRepository.openInstagram()
            }

            AboutUsOption.MAIL -> {
                shareRepository.sendEmail(null)
            }

            AboutUsOption.LICENSES -> {
                viewModel.processAction(AboutAction.OpenLicense)
            }

            AboutUsOption.BACK -> {
                viewModel.processAction(AboutAction.ClosePage)
            }
        }
    }
}

@Composable
private fun AboutUsScreenScaffoldView(
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {
    EmScaffold(
        topBar = {
            EmTopBar(
                title = stringResource(id = R.string.about_us),
                navigation = EmTopBarNavigation.Back,
                onNavigationClick = {
                    settingOptionSelected?.invoke(AboutUsOption.BACK)
                },
            )
        },
    ) { innerPadding ->
        AboutUsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            settingOptionSelected = settingOptionSelected,
        )
    }
}

@Composable
private fun AboutUsScreenContent(
    modifier: Modifier = Modifier,
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        EmCard(
            modifier = Modifier.padding(
                horizontal = EmTheme.spacing.lg,
                vertical = EmTheme.spacing.sm,
            ),
        ) {
            EmSettingItem(
                icon = Icons.Outlined.Info,
                title = stringResource(id = R.string.about_us),
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.ABOUT_US)
                },
            )
            EmDivider()
            EmSettingItem(
                icon = Icons.Outlined.Policy,
                title = stringResource(id = R.string.terms_and_conditions),
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.TERMS)
                },
            )
            EmDivider()
            EmSettingItem(
                icon = Icons.Outlined.PrivacyTip,
                title = stringResource(id = R.string.privacy_policy),
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.PRIVACY_POLICY)
                },
            )
            EmDivider()
            EmSettingItem(
                icon = Icons.Outlined.FolderOpen,
                title = stringResource(id = R.string.licenses),
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.LICENSES)
                },
            )
        }
        EmCard(
            modifier = Modifier.padding(
                horizontal = EmTheme.spacing.lg,
                vertical = EmTheme.spacing.sm,
            ),
        ) {
            DeveloperInfoView(settingOptionSelected)
        }
    }
}

@Composable
private fun DeveloperInfoView(
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(EmTheme.spacing.lg),
    ) {
        EmText(
            text = stringResource(id = R.string.developed_by),
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
        )
        Row(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = EmTheme.spacing.lg),
        ) {
            EmIconButton(
                painter = painterResource(id = R.drawable.ic_github),
                contentDescription = stringResource(id = R.string.about_github),
                onClick = { settingOptionSelected?.invoke(AboutUsOption.GITHUB) },
            )
            EmIconButton(
                painter = painterResource(id = R.drawable.ic_twitter),
                contentDescription = stringResource(id = R.string.about_twitter),
                onClick = { settingOptionSelected?.invoke(AboutUsOption.TWITTER) },
            )
            EmIconButton(
                painter = painterResource(id = R.drawable.ic_instagram),
                contentDescription = stringResource(id = R.string.about_instagram),
                onClick = { settingOptionSelected?.invoke(AboutUsOption.INSTAGRAM) },
            )
            EmIconButton(
                painter = painterResource(id = R.drawable.ic_mail),
                contentDescription = stringResource(id = R.string.about_email),
                onClick = { settingOptionSelected?.invoke(AboutUsOption.MAIL) },
            )
        }
        EmText(
            text = stringResource(
                id = R.string.app_version,
                runCatching {
                    context.getAppVersionName()
                }.getOrNull() ?: "1.0.0",
            ),
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = EmTheme.spacing.lg),
        )
    }
}

private enum class AboutUsOption {
    BACK,
    ABOUT_US,
    TERMS,
    PRIVACY_POLICY,
    LICENSES,
    GITHUB,
    TWITTER,
    INSTAGRAM,
    MAIL,
}

@Composable
@EmPreviews
fun AboutUsScreenScaffoldViewPreview() {
    ExpenseManagerPreviewTheme(padding = 0.dp) {
        AboutUsScreenScaffoldView { }
    }
}
