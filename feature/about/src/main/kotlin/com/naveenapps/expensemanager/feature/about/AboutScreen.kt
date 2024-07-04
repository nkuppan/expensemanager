package com.naveenapps.expensemanager.feature.about

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.TopNavigationBar
import com.naveenapps.expensemanager.core.designsystem.ui.extensions.getAppVersionName
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.repository.ShareRepository

@Composable
fun AboutScreen(
    shareRepository: ShareRepository,
    viewModel: AboutUsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    AboutUsScreenScaffoldView(
        onAction = {
            when (it) {
                AboutAction.OpenAboutUsPage -> {
                    shareRepository.openAboutUs()
                }

                AboutAction.OpenTerms -> {
                    shareRepository.openTerms()
                }

                AboutAction.OpenPrivacy -> {
                    shareRepository.openPrivacy()
                }

                AboutAction.Github -> {
                    shareRepository.openGithub()
                }

                AboutAction.Twitter -> {
                    shareRepository.openTwitter()
                }

                AboutAction.Instagram -> {
                    shareRepository.openInstagram()
                }

                AboutAction.Mail -> {
                    shareRepository.sendEmail(null)
                }

                AboutAction.OpenLicense -> {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }

                else -> {
                    viewModel.processAction(it)
                }
            }
        }
    )
}

@Composable
private fun AboutUsScreenScaffoldView(
    onAction: (AboutAction) -> Unit,
) {

    Scaffold(
        topBar = {
            TopNavigationBar(
                onClick = { onAction.invoke(AboutAction.ClosePage) },
                title = stringResource(R.string.about_us),
            )
        },
    ) { innerPadding ->
        AboutUsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            onAction
        )
    }
}

@Composable
private fun AboutUsScreenContent(
    modifier: Modifier = Modifier,
    onAction: (AboutAction) -> Unit,
) {
    Column(modifier = modifier) {
        SettingsItem(
            modifier = Modifier
                .clickable {
                    onAction.invoke(AboutAction.OpenAboutUsPage)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.about_us),
            icon = Icons.Outlined.Info,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    onAction.invoke(AboutAction.OpenTerms)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.terms_and_conditions),
            icon = Icons.Outlined.Policy,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    onAction.invoke(AboutAction.OpenPrivacy)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.privacy_policy),
            icon = Icons.Outlined.PrivacyTip,
        )
        SettingsItem(
            modifier = Modifier
                .clickable {
                    onAction.invoke(AboutAction.OpenLicense)
                }
                .padding(top = 8.dp, bottom = 8.dp)
                .fillMaxWidth(),
            title = stringResource(id = R.string.licenses),
            icon = Icons.Outlined.FolderOpen,
        )

        DeveloperInfoView(onAction)
    }
}

@Composable
private fun DeveloperInfoView(
    onAction: (AboutAction) -> Unit,
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
                onAction.invoke(AboutAction.Github)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_github),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                onAction.invoke(AboutAction.Twitter)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_twitter),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                onAction.invoke(AboutAction.Instagram)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_instagram),
                    contentDescription = "",
                )
            }
            IconButton(onClick = {
                onAction.invoke(AboutAction.Mail)
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
                context.getAppVersionName(),
            ),
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    Row(modifier = modifier) {
        Icon(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            imageVector = icon,
            contentDescription = null,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
        ) {
            Text(text = title)
            if (description?.isNotBlank() == true) {
                Text(text = description, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun AboutUsPreview() {
    ExpenseManagerTheme {
        AboutUsScreenScaffoldView {

        }
    }
}
