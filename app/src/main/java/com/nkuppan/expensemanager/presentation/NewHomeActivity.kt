package com.nkuppan.expensemanager.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.feature.category.create.CategoryCreateScreen
import com.nkuppan.expensemanager.feature.category.create.CategoryCreateViewModel
import com.nkuppan.expensemanager.feature.category.list.CategoryListScreen
import com.nkuppan.expensemanager.feature.category.list.CategoryListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainPageView()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageView() {

    val navController = rememberNavController()

    Scaffold {
        NavHost(
            modifier = Modifier.padding(
                bottom = it.calculateTopPadding()
            ),
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .align(Alignment.Center)
                    ) {
                        Button(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally),
                            onClick = {
                                navController.navigate("category")
                            }) {
                            Text(text = "Navigate to Category")
                        }
                        Button(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally), onClick = {
                                navController.navigate("category/create")
                            }) {
                            Text(text = "Navigate to Category Create")
                        }
                        Button(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally), onClick = {
                                navController.navigate("transaction")
                            }) {
                            Text(text = "Navigate to Transaction")
                        }
                        Button(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally), onClick = {
                                navController.navigate("analysis")
                            }) {
                            Text(text = "Navigate to Analysis")
                        }
                        Button(
                            modifier = Modifier
                                .wrapContentSize()
                                .align(Alignment.CenterHorizontally),
                            onClick = {
                                navController.navigate("settings")
                            }) {
                            Text(text = "Navigate to Settings")
                        }
                    }
                }
            }
            composable("category") {
                CategoryListScreen(navController)
            }
            composable("category/create") {
                CategoryCreateScreen(navController)
            }
            composable("transaction") {

            }
            composable("analysis") {

            }
            composable("settings") {

            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryListScreen(
    navController: NavController
) {
    val viewModel: CategoryListViewModel = hiltViewModel()
    val categoryUiState by viewModel.categories.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.category))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("category/create")
            }) {
                Image(
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_add),
                    contentDescription = ""
                )
            }
        }
    ) {
        CategoryListScreen(
            modifier = Modifier.padding(top = it.calculateTopPadding()),
            categoryUiState = categoryUiState
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CategoryCreateScreen(
    navController: NavController
) {
    val viewModel: CategoryCreateViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.category))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {

            }) {
                Image(
                    painter = painterResource(id = com.nkuppan.expensemanager.core.ui.R.drawable.ic_done),
                    contentDescription = ""
                )
            }
        }
    ) {
        CategoryCreateScreen(modifier = Modifier.padding(top = it.calculateTopPadding()))
    }
}

@Preview
@Composable
fun MainPagePreview() {

    MaterialTheme {
        MainPageView()
    }
}


