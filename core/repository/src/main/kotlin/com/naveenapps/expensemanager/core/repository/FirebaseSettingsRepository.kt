package com.naveenapps.expensemanager.core.repository

interface FirebaseSettingsRepository {

    fun getPrivacyURL(): String

    fun getTermsURL(): String

    fun getAboutUsURL(): String

    fun getGithubURL(): String

    fun getInstagramURL(): String

    fun getTwitterURL(): String

    fun getFeedbackEmail(): String

    fun getMaxBusinessProfileCount(): Long

    fun getMaxInvoicesCount(): Long

    fun getMaxClientsCount(): Long

    fun getMaxItemsCount(): Long

    fun getMaxPaymentMethodCount(): Long

    fun getMaxTermsAndConditionCount(): Long
}