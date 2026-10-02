package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CalculateStreakTest {

    private val today = 20_000L

    @Test
    fun `no logged days gives an empty streak`() {
        val streak = calculateStreak(emptySet(), today)

        assertThat(streak.current).isEqualTo(0)
        assertThat(streak.longest).isEqualTo(0)
        assertThat(streak.loggedToday).isFalse()
        assertThat(streak.lastSevenDays).containsExactly(false, false, false, false, false, false, false)
    }

    @Test
    fun `consecutive days ending today are counted`() {
        val streak = calculateStreak(setOf(today - 2, today - 1, today), today)

        assertThat(streak.current).isEqualTo(3)
        assertThat(streak.loggedToday).isTrue()
    }

    @Test
    fun `unlogged today keeps yesterday's streak alive`() {
        val streak = calculateStreak(setOf(today - 2, today - 1), today)

        assertThat(streak.current).isEqualTo(2)
        assertThat(streak.loggedToday).isFalse()
    }

    @Test
    fun `a missed day breaks the streak`() {
        val streak = calculateStreak(setOf(today - 3, today - 2, today), today)

        assertThat(streak.current).isEqualTo(1)
    }

    @Test
    fun `missing both today and yesterday resets the streak`() {
        val streak = calculateStreak(setOf(today - 4, today - 3, today - 2), today)

        assertThat(streak.current).isEqualTo(0)
        assertThat(streak.longest).isEqualTo(3)
    }

    @Test
    fun `longest streak is remembered after it ends`() {
        val days = (today - 20..today - 10).toSet() + setOf(today - 1, today)

        val streak = calculateStreak(days, today)

        assertThat(streak.current).isEqualTo(2)
        assertThat(streak.longest).isEqualTo(11)
    }

    @Test
    fun `future-dated transactions are ignored for the longest streak`() {
        val streak = calculateStreak(setOf(today, today + 1, today + 2, today + 3), today)

        assertThat(streak.current).isEqualTo(1)
        assertThat(streak.longest).isEqualTo(1)
    }

    @Test
    fun `last seven days run oldest to today`() {
        val streak = calculateStreak(setOf(today - 6, today), today)

        assertThat(streak.lastSevenDays).containsExactly(true, false, false, false, false, false, true).inOrder()
    }
}
