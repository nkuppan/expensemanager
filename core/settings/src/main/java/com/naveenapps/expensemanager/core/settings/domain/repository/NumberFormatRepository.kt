package com.naveenapps.expensemanager.core.settings.domain.repository

interface NumberFormatRepository {

    fun formatForDisplay(value: Double): String

    fun formatForEditing(localizedValue: String): String

    fun formatForEditing(value: Double): String

    fun parseToDouble(localizedValue: String): Double?
}