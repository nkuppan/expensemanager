package com.naveenapps.expensemanager.feature.about

import androidx.lifecycle.ViewModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutUsViewModel @Inject constructor(
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}
