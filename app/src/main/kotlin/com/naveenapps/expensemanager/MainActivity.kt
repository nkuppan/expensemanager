package com.naveenapps.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.ui.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
internal class HomeActivity : ComponentActivity() {

    @Inject
    internal lateinit var appComposeNavigator: AppComposeNavigator

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onboardingStatus.collectLatest {
                    splashScreen.setKeepOnScreenCondition {
                        it == null
                    }
                }
            }
        }

        setContent {

            val currentTheme by viewModel.currentTheme.collectAsState()

            val onBoardingStatus by viewModel.onboardingStatus.collectAsState()

            val isDarkTheme = shouldUseDarkTheme(theme = currentTheme)

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = isDarkTheme.not()
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                    isNavigationBarContrastEnforced = false,
                )
            }

            if (onBoardingStatus != null) {
                MainScreen(
                    appComposeNavigator,
                    isDarkTheme,
                    if (onBoardingStatus == true) {
                        ExpenseManagerScreens.Home.name
                    } else {
                        ExpenseManagerScreens.Onboarding.name
                    }
                )
            }
        }
    }
}

/**
 * Returns `true` if dark theme should be used, as a function of the [theme] and the
 * current system context.
 */
@Composable
fun shouldUseDarkTheme(theme: Theme): Boolean = when (theme.mode) {
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> isSystemInDarkTheme()
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> {
        isSystemInDarkTheme()
    }
}