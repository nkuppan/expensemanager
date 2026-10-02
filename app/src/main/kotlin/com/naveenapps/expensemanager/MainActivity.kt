package com.naveenapps.expensemanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.CompositionLocalProvider
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
import com.naveenapps.expensemanager.core.designsystem.theme.ExpenseManagerTheme
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.core.repository.AnalyticsEvents
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.ui.AppLockScreen
import com.naveenapps.expensemanager.ui.MainScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

internal class MainActivity :
    AppCompatActivity(),
    AndroidScopeComponent {

    private val appComposeNavigator: AppComposeNavigator by inject()

    private val analyticsRepository: AnalyticsRepository by inject()

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

        QuickAdd.publishShortcut(applicationContext)

        // Only on a fresh launch: a recreated Activity re-delivers the same intent.
        if (savedInstanceState == null) {
            handleQuickAdd(intent)
        }

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
            // Provided explicitly rather than relying on the automatic ViewTree lookup that
            // rememberLauncherForActivityResult() falls back to (LocalView.current walking up to
            // the decor view's ViewTreeActivityResultRegistryOwner). That lookup can race with
            // window attachment right after this Activity is (re)created — e.g. following an
            // in-app locale switch, a config change, or process restoration — occasionally
            // resolving to null and crashing deep inside the Home screen's NavHost destination
            // with "No ActivityResultRegistryOwner was provided via LocalActivityResultRegistryOwner".
            // Providing it here up front removes that race entirely.
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides this) {
                val currentTheme by viewModel.currentTheme.collectAsState()
                val onBoardingStatus by viewModel.onboardingStatus.collectAsState()
                val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val pendingQuickAdd by viewModel.pendingQuickAdd.collectAsState()
                val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

                if (onBoardingStatus != null) {
                    val showLock = onBoardingStatus == true && isAppLockEnabled && !isAuthenticated

                    if (showLock) {
                        ExpenseManagerTheme(isDarkTheme = isDarkTheme) {
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
                                // Single first-run screen (Intro + Setup were merged).
                                ExpenseManagerScreens.Onboarding
                            },
                            // Behind the app lock this waits until MainScreen exists (unlocked).
                            // Before onboarding there's nowhere sensible to add, so just drop it.
                            pendingQuickAdd = pendingQuickAdd && onBoardingStatus == true,
                            onQuickAddHandled = viewModel::onQuickAddHandled,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleQuickAdd(intent)
    }

    /** Widget "+", launcher shortcut or reminder tap: open straight on the keypad. */
    private fun handleQuickAdd(intent: Intent?) {
        val source = QuickAdd.sourceOf(intent) ?: return
        if (source == QuickAdd.SOURCE_REMINDER) {
            analyticsRepository.logEvent(AnalyticsEvents.REMINDER_OPENED, emptyMap())
        }
        analyticsRepository.logEvent(
            AnalyticsEvents.QUICK_ADD_OPENED,
            mapOf(AnalyticsParams.SOURCE to source),
        )
        viewModel.requestQuickAdd()
    }

    override fun onStart() {
        super.onStart()
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
        // appUpdateInfo() is async (backed by a Play Core service call), so the result can land
        // after this Activity instance has already been destroyed — e.g. a config change like a
        // rotation or an in-app locale switch (AppCompatDelegate.setApplicationLocales triggers a
        // recreate). Once destroyed, activityResultLauncher is unregistered by the framework, so
        // calling launch() on it from a stale callback crashes with
        // "Attempting to launch an unregistered ActivityResultLauncher". Scoping the listener to
        // this Activity (rather than a plain addOnSuccessListener) makes Play Services drop the
        // callback automatically once the Activity stops, so it never fires against a destroyed
        // instance.
        appUpdateInfoTask.addOnSuccessListener(this) { appUpdateInfo ->
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
