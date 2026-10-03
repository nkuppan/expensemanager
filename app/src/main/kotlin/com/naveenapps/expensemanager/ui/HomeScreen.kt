package com.naveenapps.expensemanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.naveenapps.expensemanager.core.designsystem.ui.components.DockedFabCutoutShape
import com.naveenapps.expensemanager.core.designsystem.utils.BackHandler
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.feature.about.AboutScreen
import com.naveenapps.expensemanager.feature.account.create.AccountCreateScreen
import com.naveenapps.expensemanager.feature.account.list.AccountListScreen
import com.naveenapps.expensemanager.feature.account.reorder.AccountReOrderScreen
import com.naveenapps.expensemanager.feature.analysis.AnalysisScreen
import com.naveenapps.expensemanager.feature.budget.create.BudgetCreateScreen
import com.naveenapps.expensemanager.feature.budget.details.BudgetDetailScreen
import com.naveenapps.expensemanager.feature.budget.list.BudgetListScreen
import com.naveenapps.expensemanager.feature.category.create.CategoryCreateScreen
import com.naveenapps.expensemanager.feature.category.details.CategoryDetailScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryListScreen
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionTabScreen
import com.naveenapps.expensemanager.feature.currency.CurrencyCustomiseScreen
import com.naveenapps.expensemanager.feature.dashboard.DashboardScreen
import com.naveenapps.expensemanager.feature.export.ExportScreen
import com.naveenapps.expensemanager.feature.onboarding.OnboardingScreen
import com.naveenapps.expensemanager.feature.onboarding.into.IntroScreen
import com.naveenapps.expensemanager.feature.reminder.ReminderScreen
import com.naveenapps.expensemanager.feature.settings.SettingsScreen
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsScreen
import com.naveenapps.expensemanager.feature.transaction.R as TransactionR
import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateScreen
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomePageNavHostContainer(
    backupRepository: ActivityComponentProvider,
    navHostController: NavHostController,
    landingScreen: ExpenseManagerScreens,
) {
    NavHost(
        navController = navHostController,
        startDestination = landingScreen,
    ) {
        this.expenseManagerNavigation(backupRepository)
    }
}

fun NavGraphBuilder.expenseManagerNavigation(
    componentProvider: ActivityComponentProvider,
) {
    composable<ExpenseManagerScreens.IntroScreen> {
        IntroScreen(componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.Onboarding> {
        OnboardingScreen(componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.Home> {
        HomeScreen()
    }
    composable<ExpenseManagerScreens.CategoryList> {
        CategoryListScreen()
    }
    composable<ExpenseManagerScreens.CategoryCreate> {
        CategoryCreateScreen()
    }
    composable<ExpenseManagerScreens.CategoryDetails> {
        CategoryDetailScreen()
    }
    composable<ExpenseManagerScreens.TransactionList> {
        TransactionListScreen(showBackNavigationIcon = true)
    }
    composable<ExpenseManagerScreens.TransactionCreate> {
        TransactionCreateScreen(shareRepository = componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.AccountList> {
        AccountListScreen()
    }
    composable<ExpenseManagerScreens.AccountCreate> {
        AccountCreateScreen()
    }
    composable<ExpenseManagerScreens.BudgetList> {
        BudgetListScreen()
    }
    composable<ExpenseManagerScreens.BudgetCreate> {
        BudgetCreateScreen()
    }
    composable<ExpenseManagerScreens.BudgetDetails> {
        BudgetDetailScreen()
    }
    composable<ExpenseManagerScreens.AnalysisScreen> {
        AnalysisScreen()
    }
    composable<ExpenseManagerScreens.Settings> {
        SettingsScreen(
            shareRepository = componentProvider.getShareRepository(),
        )
    }
    composable<ExpenseManagerScreens.ExportScreen> {
        ExportScreen()
    }
    composable<ExpenseManagerScreens.ReminderScreen> {
        ReminderScreen(
            shareRepository = componentProvider.getShareRepository(),
        )
    }
    composable<ExpenseManagerScreens.CurrencyCustomiseScreen> {
        CurrencyCustomiseScreen()
    }
    composable<ExpenseManagerScreens.CategoryTransaction> {
        CategoryTransactionTabScreen()
    }
    composable<ExpenseManagerScreens.AboutUsScreen> {
        AboutScreen(componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.AdvancedSettingsScreen> {
        AdvancedSettingsScreen()
    }
    composable<ExpenseManagerScreens.AccountReOrderScreen> {
        AccountReOrderScreen()
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalActivity.current

    val homeScreenBottomBarItems by viewModel.homeScreenBottomBarItems.collectAsState()

    val appContext = LocalContext.current

    fun hasNotificationPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

    var canNotify by remember { mutableStateOf(hasNotificationPermission()) }
    val shouldOfferReminderPrimer by viewModel.shouldOfferReminderPrimer.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            canNotify = granted
            viewModel.onNotificationPermissionResult(granted)
            if (granted) {
                // The user just said yes in the system dialog, so switching the reminder on is
                // what they asked for.
                viewModel.turnOnNotification()
            }
        },
    )

    LaunchedEffect(Unit) {
        // Never force the reminder on and never ask cold on launch. If we can already notify,
        // only reschedule (which respects a reminder the user switched off). Otherwise wait:
        // the explainer below asks once, after the first saved transaction.
        if (canNotify) {
            viewModel.restartReminderIfEnabled()
        }
    }

    val showFirstSaveCelebration by viewModel.showFirstSaveCelebration.collectAsState()

    if (showFirstSaveCelebration) {
        LaunchedEffect(Unit) { viewModel.onFirstSaveCelebrationShown() }
        FirstSaveCelebrationSheet(
            onAddAnother = viewModel::onFirstSaveAddAnother,
            onDone = viewModel::onFirstSaveDone,
        )
    } else if (shouldOfferReminderPrimer && !canNotify) {
        // Never stacked on the celebration; the reminder sheet waits until it's dismissed.
        LaunchedEffect(Unit) { viewModel.onReminderPrimerShown() }
        ReminderPrimerSheet(
            onTurnOn = {
                viewModel.onReminderPrimerAccepted()
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            },
            onNotNow = viewModel::onReminderPrimerDismissed,
        )
    }

    BackHandler {
        if (homeScreenBottomBarItems != HomeScreenBottomBarItems.Home) {
            viewModel.setUISystem(HomeScreenBottomBarItems.Home)
        } else {
            context?.finish()
        }
    }

    Scaffold(
        bottomBar = {
            // Bar with a centred notch; the shared "+" FAB below docks into it. One add action
            // for every tab instead of a differently-styled FAB per tab.
            val tabs = HomeScreenBottomBarItems.entries
            val half = tabs.size / 2
            Surface(
                shape = DockedFabCutoutShape(fabDiameter = FabDiameter, margin = FabCutoutMargin),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 8.dp,
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                ) {
                    tabs.take(half).forEach { uiSystem ->
                        HomeTabItem(uiSystem, homeScreenBottomBarItems == uiSystem) {
                            viewModel.setUISystem(uiSystem)
                        }
                    }
                    // Empty middle slot: the FAB sits here, in the notch.
                    Spacer(modifier = Modifier.weight(1f))
                    tabs.drop(half).forEach { uiSystem ->
                        HomeTabItem(uiSystem, homeScreenBottomBarItems == uiSystem) {
                            viewModel.setUISystem(uiSystem)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddTransaction,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // M3 Scaffold places the FAB 16dp above the bottom bar; push it down by that
                // gap plus its own radius so its centre sits on the bar's top edge (docked).
                modifier = Modifier
                    .size(FabDiameter)
                    .offset(y = ScaffoldFabSpacing + FabDiameter / 2),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(TransactionR.string.add_transaction),
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(
                bottom = paddingValues.calculateBottomPadding(),
            ),
        ) {
            when (homeScreenBottomBarItems) {
                HomeScreenBottomBarItems.Home -> {
                    DashboardScreen()
                }

                HomeScreenBottomBarItems.Analysis -> {
                    AnalysisScreen()
                }

                HomeScreenBottomBarItems.Transaction -> {
                    TransactionListScreen(showAddButton = false)
                }

                HomeScreenBottomBarItems.Category -> {
                    CategoryTransactionTabScreen(showAddButton = false)
                }
            }
        }
    }
}

@Composable
private fun RowScope.HomeTabItem(
    item: HomeScreenBottomBarItems,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                painterResource(item.iconResourceID),
                stringResource(item.labelResourceID),
            )
        },
        label = { Text(stringResource(item.labelResourceID)) },
    )
}

private val FabDiameter = 56.dp
private val FabCutoutMargin = 6.dp

// Matches Material 3 Scaffold's internal FabSpacing (gap between FAB and bottom bar).
private val ScaffoldFabSpacing = 16.dp
