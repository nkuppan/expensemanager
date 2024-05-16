package com.naveenapps.expensemanager.ui

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.naveenapps.expensemanager.core.designsystem.utils.BackHandler
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.BackupRepository
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
import com.naveenapps.expensemanager.feature.reminder.ReminderScreen
import com.naveenapps.expensemanager.feature.settings.AdvancedSettingsScreen
import com.naveenapps.expensemanager.feature.settings.SettingsScreen
import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateScreen
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListScreen

@Composable
fun HomePageNavHostContainer(
    backupRepository: BackupRepository,
    navHostController: NavHostController,
    landingScreen: ExpenseManagerScreens,
) {
    NavHost(
        navController = navHostController,
        startDestination = landingScreen,
    ) {
        this.expenseManagerNavigation(
            backupRepository
        )
    }
}

fun NavGraphBuilder.expenseManagerNavigation(
    backupRepository: BackupRepository
) {
    composable<ExpenseManagerScreens.Onboarding> {
        OnboardingScreen()
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
        TransactionListScreen()
    }
    composable<ExpenseManagerScreens.TransactionCreate> {
        TransactionCreateScreen()
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
        SettingsScreen()
    }
    composable<ExpenseManagerScreens.ExportScreen> {
        ExportScreen()
    }
    composable<ExpenseManagerScreens.ReminderScreen> {
        ReminderScreen()
    }
    composable<ExpenseManagerScreens.CurrencyCustomiseScreen> {
        CurrencyCustomiseScreen()
    }
    composable<ExpenseManagerScreens.CategoryTransaction> {
        CategoryTransactionTabScreen()
    }
    composable<ExpenseManagerScreens.AboutUsScreen> {
        AboutScreen()
    }
    composable<ExpenseManagerScreens.AdvancedSettingsScreen> {
        AdvancedSettingsScreen(backupRepository = backupRepository)
    }
    composable<ExpenseManagerScreens.AccountReOrderScreen> {
        AccountReOrderScreen()
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val homeScreenBottomBarItems by viewModel.homeScreenBottomBarItems.collectAsState()

    var hasNotificationPermission by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            hasNotificationPermission = it
            if (it) {
                viewModel.turnOnNotification()
            }
        }
    )

    LaunchedEffect(key1 = "permission") {
        if (hasNotificationPermission.not()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.turnOnNotification()
            }
        }
    }

    BackHandler {
        if (homeScreenBottomBarItems != HomeScreenBottomBarItems.Home) {
            viewModel.setUISystem(HomeScreenBottomBarItems.Home)
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                HomeScreenBottomBarItems.entries.forEach { uiSystem ->
                    NavigationBarItem(
                        selected = homeScreenBottomBarItems == uiSystem,
                        onClick = { viewModel.setUISystem(uiSystem) },
                        icon = {
                            Icon(
                                painterResource(uiSystem.iconResourceID),
                                stringResource(uiSystem.labelResourceID),
                            )
                        },
                        label = { Text(stringResource(uiSystem.labelResourceID)) },
                    )
                }
            }
        },
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
                    TransactionListScreen()
                }

                HomeScreenBottomBarItems.Category -> {
                    CategoryTransactionTabScreen()
                }
            }
        }
    }
}
