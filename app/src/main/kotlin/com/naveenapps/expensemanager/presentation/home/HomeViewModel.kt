package com.naveenapps.expensemanager.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    var homeScreenBottomBarItems by mutableStateOf(HomeScreenBottomBarItems.Home)
        private set

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        this.homeScreenBottomBarItems = homeScreenBottomBarItems
    }
}