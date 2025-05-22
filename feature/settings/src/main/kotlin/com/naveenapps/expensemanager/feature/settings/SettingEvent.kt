package com.naveenapps.expensemanager.feature.settings

sealed class SettingEvent {

    data object RateUs : SettingEvent()
}