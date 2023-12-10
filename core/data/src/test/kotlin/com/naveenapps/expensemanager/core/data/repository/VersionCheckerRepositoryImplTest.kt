package com.naveenapps.expensemanager.core.data.repository

import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class VersionCheckerRepositoryImplTest : BaseCoroutineTest() {

    private val versionCheckerRepository: VersionCheckerRepository = mock()

    @Test
    fun whenAccessingAndroidQAndAboveShouldReturnTheTrue() = runTest {
        whenever(versionCheckerRepository.isAndroidQAndAbove()).thenReturn(true)
        Truth.assertThat(versionCheckerRepository.isAndroidQAndAbove()).isTrue()
    }

    @Test
    fun whenAccessingAndroidQAndAboveShouldReturnTheFalse() = runTest {
        whenever(versionCheckerRepository.isAndroidQAndAbove()).thenReturn(false)
        Truth.assertThat(versionCheckerRepository.isAndroidQAndAbove()).isFalse()
    }
}
