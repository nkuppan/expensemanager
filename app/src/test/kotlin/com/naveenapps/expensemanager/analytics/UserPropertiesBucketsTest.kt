package com.naveenapps.expensemanager.analytics

import androidx.appcompat.app.AppCompatDelegate
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserPropertiesBucketsTest {

    @Test
    fun transactionsAreBucketed() {
        val buckets = listOf(0, 1, 2, 5, 6, 20, 21, 100, 101, 5000).map { UserPropertiesTracker.transactionsBucket(it) }

        assertThat(buckets).containsExactly("0", "1", "2-5", "2-5", "6-20", "6-20", "21-100", "21-100", "100+", "100+")
            .inOrder()
    }

    @Test
    fun accountsAreBucketedAtFive() {
        assertThat((0..7).map { UserPropertiesTracker.accountsBucket(it) })
            .containsExactly("0", "1", "2", "3", "4", "5+", "5+", "5+").inOrder()
    }

    @Test
    fun themeModesHaveReadableNames() {
        assertThat(UserPropertiesTracker.themeName(AppCompatDelegate.MODE_NIGHT_YES)).isEqualTo("dark")
        assertThat(UserPropertiesTracker.themeName(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)).isEqualTo("system")
        assertThat(UserPropertiesTracker.themeName(42)).isEqualTo("other")
    }
}
