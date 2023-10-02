package com.nkuppan.expensemanager.core.ui.theme.widget

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.nkuppan.expensemanager.R

@Composable
fun NavigationButton(
    navController: NavController,
    navigationIcon: Int = R.drawable.ic_arrow_back,
    navigationContentDescription: String = ""
) {
    IconButton(
        onClick = {
            navController.popBackStack()
        }
    ) {
        Icon(
            painter = painterResource(id = navigationIcon),
            contentDescription = navigationContentDescription
        )
    }
}