package com.naveenapps.expensemanager.feature.onboarding.into

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.naveenapps.expensemanager.core.designsystem.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.onboarding.R

@Composable
fun IntroScreen(
    shareRepository: ShareRepository?,
    viewModel: IntroViewModel = hiltViewModel()
) {
    ScaffoldContent(viewModel::navigate, shareRepository)
}

@Composable
private fun ScaffoldContent(
    navigate: () -> Unit,
    shareRepository: ShareRepository?
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.expense_1))

                    LottieAnimation(
                        composition = composition,
                        reverseOnRepeat = true,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_message_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.welcome_message_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                onClick = {
                    navigate.invoke()
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(text = stringResource(id = R.string.get_started).uppercase())
            }

            Text(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
                    .fillMaxWidth(),
                text = buildAnnotatedString {
                    val text = stringResource(id = R.string.privacy_text)
                    append(text)
                    addLink(
                        url = LinkAnnotation.Url(
                            url = "",
                            linkInteractionListener = {
                                shareRepository?.openPrivacy()
                            },
                            style = SpanStyle(color = MaterialTheme.colorScheme.secondary)
                        ),
                        start = text.indexOf("privacy policy"),
                        end = text.length - 1,
                    )
                },
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}


@AppPreviewsLightAndDarkMode
@Composable
fun IntroScreenPreview() {
    ExpenseManagerTheme {
        ScaffoldContent({}, null)
    }
}