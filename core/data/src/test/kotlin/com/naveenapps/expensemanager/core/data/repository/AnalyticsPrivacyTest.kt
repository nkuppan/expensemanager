package com.naveenapps.expensemanager.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.repository.AnalyticsParams
import com.naveenapps.expensemanager.core.repository.AnalyticsPrivacy
import org.junit.Test

class AnalyticsPrivacyTest {

    @Test
    fun moneyLikeNamesAreBlocked() {
        listOf("amount", "found_amount", "total_spent", "balance", "price", "budget_threshold", "percent", "income")
            .forEach { assertThat(AnalyticsPrivacy.isBlocked(it)).isTrue() }
    }

    @Test
    fun theAppsOwnParamsAndPropertiesPass() {
        listOf(
            AnalyticsParams.TYPE, AnalyticsParams.SOURCE, AnalyticsParams.METHOD, AnalyticsParams.COUNT,
            AnalyticsParams.TRANSACTION_COUNT, AnalyticsParams.ACCOUNT_COUNT, AnalyticsParams.CATEGORY_COUNT,
            AnalyticsParams.STAGE, AnalyticsParams.FIELDS, AnalyticsParams.SECONDS, AnalyticsParams.REASON,
            "transactions_logged", "reminder_on", "has_budget", "widget_placed", "accounts", "currency_code",
        ).forEach { assertThat(AnalyticsPrivacy.isBlocked(it)).isFalse() }
    }

    @Test
    fun filterDropsOnlyTheBlockedParams() {
        val filtered = AnalyticsPrivacy.filter(mapOf("type" to "expense", "amount" to "250"))

        assertThat(filtered).containsExactly("type", "expense")
    }
}
