package com.naveenapps.expensemanager.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.naveenapps.expensemanager.core.designsystem.ui.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.presentation.home.TempMainPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {

            val themeViewModel: com.naveenapps.expensemanager.feature.theme.ThemeViewModel =
                hiltViewModel()
            val currentTheme by themeViewModel.currentTheme.collectAsState()

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

            ExpenseManagerTheme(isDarkTheme = isDarkTheme) {
                TempMainPageView()
            }
        }
    }
}

/**
 * Returns `true` if dark theme should be used, as a function of the [theme] and the
 * current system context.
 */
@Composable
private fun shouldUseDarkTheme(theme: Theme): Boolean = when (theme.mode) {
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> isSystemInDarkTheme()
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> {
        isSystemInDarkTheme()
    }
}


