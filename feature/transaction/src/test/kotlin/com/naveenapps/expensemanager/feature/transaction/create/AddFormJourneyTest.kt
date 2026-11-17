package com.naveenapps.expensemanager.feature.transaction.create

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AddFormJourneyTest {

    @Test
    fun leavingWithoutChangingAnythingIsUntouched() {
        val journey = AddFormJourney(startedAt = 0L)

        assertThat(journey.stage).isEqualTo(AddFormJourney.STAGE_UNTOUCHED)
        assertThat(journey.fields).isEqualTo("none")
    }

    @Test
    fun changingAnyFieldCountsAsDetails() {
        val journey = AddFormJourney(0L).apply { touch(AddFormJourney.FIELD_CATEGORY) }

        assertThat(journey.stage).isEqualTo(AddFormJourney.STAGE_DETAILS)
    }

    @Test
    fun fieldsAreListedOnceInTheOrderFirstChanged() {
        val journey = AddFormJourney(0L).apply {
            touch(AddFormJourney.FIELD_NOTES)
            touch(AddFormJourney.FIELD_CATEGORY)
            touch(AddFormJourney.FIELD_NOTES)
            touch(AddFormJourney.FIELD_DATE)
        }

        assertThat(journey.fields).isEqualTo("notes,category,date")
    }

    @Test
    fun countsBlockedSavesAndSecondsOnScreen() {
        val journey = AddFormJourney(startedAt = 10_000L).apply {
            onSaveBlocked()
            onSaveBlocked()
        }

        assertThat(journey.blockedSaves).isEqualTo(2)
        assertThat(journey.seconds(now = 25_500L)).isEqualTo(15L)
        assertThat(journey.seconds(now = 5_000L)).isEqualTo(0L)
    }
}
