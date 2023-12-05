package com.naveenapps.expensemanager.feature.account.reorder

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountReOrderViewModel @Inject constructor(
    private val appComposeNavigator: AppComposeNavigator
) : ViewModel() {

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}