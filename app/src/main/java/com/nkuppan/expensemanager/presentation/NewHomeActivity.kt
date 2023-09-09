package com.nkuppan.expensemanager.presentation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nkuppan.expensemanager.presentation.category.create.CategoryCreateScreen
import com.nkuppan.expensemanager.presentation.category.list.CategoryListScreen
import com.nkuppan.expensemanager.presentation.transaction.list.TransactionListScreen
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
                HomePageScreen(navController)
            }
            composable("category") {
                CategoryListScreen(navController)
            }
            composable("category/create") {
                CategoryCreateScreen(navController)
            }
            composable("transaction") {
                TransactionListScreen(navController)
            }
            composable("transaction/create") {

            }
            composable("analysis") {

            }
            composable("settings") {

            }
        }
    }
}

@Composable
private fun HomePageScreen(navController: NavHostController) {

    val activity = LocalContext.current as Activity

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
            Button(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    activity.startActivity(Intent(activity, HomeActivity::class.java))
                    activity.finish()
                }) {
                Text(text = "Navigate to Old App")
            }
        }
    }
}

@Preview
@Composable
fun MainPagePreview() {

    MaterialTheme {
        MainPageView()
    }
}


