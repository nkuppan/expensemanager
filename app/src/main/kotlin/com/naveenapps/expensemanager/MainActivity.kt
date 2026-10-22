package com.naveenapps.expensemanager

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.naveenapps.expensemanager.core.notification.ACTION_OPEN_AUTO_BACKUP
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

        QuickAdd.publishShortcut(applicationContext)

        // Only on a fresh launch: a recreated Activity re-delivers the same intent.
        if (savedInstanceState == null) {
            handleQuickAdd(intent)
            handleOpenAutoBackup(intent)
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

        // With app lock on, keep balances out of the recent-apps preview and screenshots.
        lifecycleScope.launch {
            viewModel.isAppLockEnabled.collect { enabled ->
                if (enabled) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
                val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
                val onBoardingStatus by viewModel.onboardingStatus.collectAsStateWithLifecycle()
                val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsStateWithLifecycle()
                val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
                val pendingQuickAdd by viewModel.pendingQuickAdd.collectAsStateWithLifecycle()
                val pendingOpenAutoBackup by viewModel.pendingOpenAutoBackup.collectAsStateWithLifecycle()
                val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)

                if (onBoardingStatus != null) {
                    val showLock = onBoardingStatus == true && isAppLockEnabled && !isAuthenticated

                    // The lock is drawn *over* MainScreen rather than replacing it, so
                    // re-locking after a trip to the background keeps the user's navigation
                    // back stack and any half-filled form. While locked, the content
                    // underneath is hidden from accessibility services and the lock's Surface
                    // consumes all touches.
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (showLock) Modifier.clearAndSetSemantics { } else Modifier),
                        ) {
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
                                // Behind the app lock this waits until the user unlocks.
                                // Before onboarding there's nowhere sensible to add, so drop it.
                                pendingQuickAdd = pendingQuickAdd && onBoardingStatus == true && !showLock,
                                onQuickAddHandled = viewModel::onQuickAddHandled,
                                // Same gating as quick add: after onboarding, and once unlocked.
                                pendingOpenAutoBackup = pendingOpenAutoBackup && onBoardingStatus == true && !showLock,
                                onOpenAutoBackupHandled = viewModel::onOpenAutoBackupHandled,
                            )
                        }

                        if (showLock) {
                            ExpenseManagerTheme(isDarkTheme = isDarkTheme) {
                                // Back must not reach the screens underneath the lock.
                                BackHandler { finish() }
                                LaunchedEffect(Unit) { showBiometricPrompt() }
                                AppLockScreen(onUnlockClick = ::showBiometricPrompt)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleQuickAdd(intent)
        handleOpenAutoBackup(intent)
    }

    /** "Automatic backup failed" notification: open the screen where it can be fixed. */
    private fun handleOpenAutoBackup(intent: Intent?) {
        if (intent?.action != ACTION_OPEN_AUTO_BACKUP) return
        analyticsRepository.logEvent(
            AnalyticsEvents.AUTO_BACKUP_FIX_OPENED,
            mapOf(AnalyticsParams.SOURCE to "notification"),
        )
        viewModel.requestOpenAutoBackup()
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
        viewModel.onAppForegrounded(SystemClock.elapsedRealtime())
        launchAppUpdateCheck()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            viewModel.onAppBackgrounded(SystemClock.elapsedRealtime())
        }
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
