package com.naveenapps.expensemanager.feature.settings

import androidx.annotation.StringRes

sealed class SettingEvent {

    data object RateUs : SettingEvent()

    data object ShareApp : SettingEvent()

    data class ShowMessage(@StringRes val messageResId: Int) : SettingEvent()
}
