package com.naveenapps.expensemanager.core.domain.usecase.settings.reminder

import com.naveenapps.expensemanager.core.model.ReminderTimeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SuggestReminderTimeTest {

    private fun at(hour: Int, minute: Int) = hour * 60 + minute

    @Test
    fun tooFewSavesGiveNoSuggestion() {
        assertNull(SuggestReminderTime.from(listOf(at(20, 0), at(21, 0)), is24Hour = false))
    }

    @Test
    fun medianIsRoundedDownToTheHalfHour() {
        val saves = listOf(at(21, 40), at(20, 50), at(21, 10))
        assertEquals(ReminderTimeState(21, 0, false), SuggestReminderTime.from(saves, is24Hour = false))
    }

    @Test
    fun oneOddSaveDoesNotMoveTheSuggestion() {
        val saves = listOf(at(13, 45), at(13, 20), at(2, 0), at(13, 50))
        assertEquals(ReminderTimeState(13, 0, true), SuggestReminderTime.from(saves, is24Hour = true))
    }

    @Test
    fun lateNightSavesAreKeptToTheEvening() {
        val saves = listOf(at(23, 30), at(23, 50), at(23, 10))
        assertEquals(ReminderTimeState(22, 0, false), SuggestReminderTime.from(saves, is24Hour = false))
    }

    @Test
    fun earlyMorningSavesAreKeptToTheMorning() {
        val saves = listOf(at(6, 15), at(6, 40), at(7, 5))
        assertEquals(ReminderTimeState(8, 0, false), SuggestReminderTime.from(saves, is24Hour = false))
    }
}
