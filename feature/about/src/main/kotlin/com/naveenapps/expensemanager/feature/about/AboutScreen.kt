package com.naveenapps.expensemanager.feature.about

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.getAppVersionName
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.repository.ShareRepository

@Composable
fun AboutScreen(
    shareRepository: ShareRepository,
    viewModel: AboutUsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val context = LocalContext.current

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
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
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

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(id = R.string.about_us),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    settingOptionSelected?.invoke(AboutUsOption.BACK)
                }
            )
        },
    ) { innerPadding ->
        AboutUsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            settingOptionSelected = settingOptionSelected
        )
    }
}

@Composable
private fun AboutUsScreenContent(
    modifier: Modifier = Modifier,
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        AppCardView(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        ) {
            SettingRow(
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.ABOUT_US)
                },
                title = stringResource(id = R.string.about_us),
                icon = Icons.Outlined.Info,
                showDivider = true,
            )
            SettingRow(
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.TERMS)
                },
                title = stringResource(id = R.string.terms_and_conditions),
                icon = Icons.Outlined.Policy,
                showDivider = true,
            )
            SettingRow(
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.PRIVACY_POLICY)
                },
                title = stringResource(id = R.string.privacy_policy),
                icon = Icons.Outlined.PrivacyTip,
                showDivider = true,
            )
            SettingRow(
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.LICENSES)
                },
                title = stringResource(id = R.string.licenses),
                icon = Icons.Outlined.FolderOpen,
            )
        }
        AppCardView(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
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
            .padding(16.dp),
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = R.string.developed_by),
        )
        Row(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
        ) {
            IconButton(onClick = {
                settingOptionSelected?.invoke(AboutUsOption.GITHUB)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(AboutUsOption.TWITTER)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_twitter),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(AboutUsOption.INSTAGRAM)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                settingOptionSelected?.invoke(AboutUsOption.MAIL)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mail),
                    contentDescription = "",
                )
            }
        }
        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp),
            text = stringResource(
                id = R.string.app_version,
                runCatching {
                    context.getAppVersionName()
                }.getOrNull() ?: "1.0.0",
            ),
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
@AppPreviewsLightAndDarkMode
fun AboutUsScreenScaffoldViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AboutUsScreenScaffoldView { }
    }
}
