package com.naveenapps.expensemanager

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.naveenapps.designsystem.theme.NaveenAppsTheme
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.ui.AppLockScreen
import com.naveenapps.expensemanager.ui.MainScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

internal class MainActivity : FragmentActivity(), AndroidScopeComponent {

    private val appComposeNavigator: AppComposeNavigator by inject()

    override val scope: Scope by activityScope()

    private val viewModel: MainViewModel by viewModel()

    private val activityComponentProvider: ActivityComponentProvider by scope.inject()

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result: ActivityResult ->
        if (result.resultCode != RESULT_OK) {
            Log.i("App", "Update flow failed! Result code: " + result.resultCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        activityComponentProvider.getBackupRepository()

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
            val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()
            val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

            if (onBoardingStatus != null) {
                val showLock = onBoardingStatus == true && isAppLockEnabled && !isAuthenticated

                if (showLock) {
                    NaveenAppsTheme(isDarkTheme = isDarkTheme) {
                        LaunchedEffect(Unit) { showBiometricPrompt() }
                        AppLockScreen(onUnlockClick = ::showBiometricPrompt)
                    }
                } else {
                    MainScreen(
                        composeNavigator = appComposeNavigator,
                        componentProvider = activityComponentProvider,
                        isDarkTheme = isDarkTheme,
                        landingScreen = if (onBoardingStatus == true) {
                            ExpenseManagerScreens.Home
                        } else {
                            ExpenseManagerScreens.IntroScreen
                        }
                    )
                }
            }
        }

        launchAppUpdateCheck()
    }

    private fun showBiometricPrompt() {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate = BiometricManager.from(this).canAuthenticate(authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            // No biometric or device credentials enrolled — bypass the lock
            viewModel.onAuthenticationSuccess()
            return
        }

        val prompt = BiometricPrompt(
            /* activity = */ this,
            /* executor = */ ContextCompat.getMainExecutor(this),
            /* callback = */ object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.onAuthenticationSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // User canceled or hardware error — lock screen stays visible for retry
                    Log.d("AppLock", "Auth error $errorCode: $errString")
                }

                override fun onAuthenticationFailed() {
                    // Biometric not recognised — lock screen stays visible for retry
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock_title))
            .setDescription(getString(R.string.app_lock_description))
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }

    private fun launchAppUpdateCheck() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
                (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                )
            }
        }
    }

    companion object {
        private const val DAYS_FOR_FLEXIBLE_UPDATE: Int = 3
    }
}
