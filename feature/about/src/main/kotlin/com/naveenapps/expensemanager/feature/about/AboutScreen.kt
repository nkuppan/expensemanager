package com.naveenapps.expensemanager.feature.about

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.repository.ShareRepository
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AboutScreen(
    shareRepository: ShareRepository,
    viewModel: AboutUsViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val state by viewModel.state.collectAsState()

    AboutUsScreenScaffoldView(
        state = state,
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
                    try {
                        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "Open source licenses are unavailable.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
    state: AboutUsState,
    onAction: (AboutAction) -> Unit,
) {

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = { onAction.invoke(AboutAction.ClosePage) },
                title = stringResource(R.string.about_us),
            )
        },
    ) { innerPadding ->
        AboutUsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun AboutUsScreenContent(
    state: AboutUsState,
    onAction: (AboutAction) -> Unit,
    modifier: Modifier = Modifier,
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

        DeveloperInfoView(state = state, onAction = onAction)
    }
}

@Composable
private fun DeveloperInfoView(
    state: AboutUsState,
    onAction: (AboutAction) -> Unit,
) {
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
                state.appVersion,
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
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AboutUsScreenScaffoldView(
            state = AboutUsState("1.0.0"),
            onAction = {}
        )
    }
}
