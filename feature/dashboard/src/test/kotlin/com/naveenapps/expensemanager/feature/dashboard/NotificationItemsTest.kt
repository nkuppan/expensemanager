package com.naveenapps.expensemanager.feature.dashboard

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Insight
import com.naveenapps.expensemanager.core.model.InsightKind
import com.naveenapps.expensemanager.feature.dashboard.notifications.NotificationItem
import com.naveenapps.expensemanager.feature.dashboard.notifications.WhatsNewRelease
import com.naveenapps.expensemanager.feature.dashboard.notifications.notificationItems
import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Test

class NotificationItemsTest {

    private val zone = ZoneOffset.UTC
    private fun at(day: Int, hour: Int) = LocalDate.of(2026, 11, day).atTime(hour, 0).toInstant(zone).toEpochMilli()

    private val releases = listOf(
        WhatsNewRelease("1.4.26", LocalDate.of(2026, 11, 7), items = 0),
        WhatsNewRelease("1.4.25", LocalDate.of(2026, 11, 3), items = 0),
    )
    private val weekly = Insight(InsightKind.WEEKLY_SUMMARY, "Your week", "₹2,000 spent", at(8, 19))
    private val budget = Insight(InsightKind.BUDGET_ALERT, "Food at 80%", "₹8,000 of ₹10,000", at(5, 10))

    @Test
    fun `releases and insights are merged newest first`() {
        val items = notificationItems(releases, listOf(weekly, budget), openedBefore = 0L, seenRelease = null, zone)

        assertThat(items.map { it.label() }).containsExactly("Your week", "1.4.26", "Food at 80%", "1.4.25").inOrder()
    }

    @Test
    fun `only what arrived since the last visit is new, and only the latest release`() {
        val items = notificationItems(
            releases,
            listOf(weekly, budget),
            openedBefore = at(6, 0),
            seenRelease = "1.4.25",
            zone = zone,
        )

        assertThat(items.filter { it.isNew }.map { it.label() }).containsExactly("Your week", "1.4.26")
    }

    @Test
    fun `a release already read is not new`() {
        val items = notificationItems(releases, emptyList(), openedBefore = 0L, seenRelease = "1.4.26", zone)

        assertThat(items.none { it.isNew }).isTrue()
    }

    private fun NotificationItem.label() = when (this) {
        is NotificationItem.Release -> release.version
        is NotificationItem.InsightItem -> insight.title
    }
}
