package com.naveenapps.expensemanager.feature.transaction.list

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchResultsBucketTest {

    @Test
    fun resultCountsAreBucketed() {
        assertThat(listOf(0, 1, 5, 6, 20, 21, 400).map { resultsBucket(it) })
            .containsExactly("0", "1-5", "1-5", "6-20", "6-20", "21+", "21+").inOrder()
    }
}
