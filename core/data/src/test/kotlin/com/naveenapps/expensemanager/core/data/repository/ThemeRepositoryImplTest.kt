package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.ThemeDataStore
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class ThemeRepositoryImplTest : BaseCoroutineTest() {

    private val versionCheckerRepository: VersionCheckerRepository = mock()

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher.dispatcher)
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = {
                testContext.preferencesDataStoreFile("TEST_DATASTORE_NAME")
            },
        )

    private val repository: ThemeRepository = ThemeRepositoryImpl(
        ThemeDataStore(testDataStore),
        versionCheckerRepository,
        AppCoroutineDispatchers(
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
            testCoroutineDispatcher.dispatcher,
        ),
    )

    @Test
    fun saveThemeShouldReturnTrue() = runTest {
        val response = repository.saveTheme(
            defaultTheme.copy(mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        )

        Truth.assertThat(response).isNotNull()
        Truth.assertThat(response).isTrue()
    }

    @Test
    fun getSelectedThemeShouldReturnDefaultTheme() = runTest {
        repository.getSelectedTheme().test {
            val theme = awaitItem()
            Truth.assertThat(theme).isNotNull()
            Truth.assertThat(theme).isEqualTo(defaultTheme)

            val modifiedTheme = defaultTheme.copy(mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            repository.saveTheme(modifiedTheme)

            val updatedTheme = awaitItem()
            Truth.assertThat(updatedTheme).isNotNull()
            Truth.assertThat(updatedTheme.mode).isEqualTo(modifiedTheme.mode)
        }
    }

    @Test
    fun getAllThemeShouldReturnFourThemeForQAndAbove() = runTest {
        whenever(versionCheckerRepository.isAndroidQAndAbove()).thenReturn(true)

        val themes = repository.getThemes()
        Truth.assertThat(themes).isNotNull()
        Truth.assertThat(themes).isNotEmpty()
        Truth.assertThat(themes).hasSize(4)
    }

    @Test
    fun getAllThemeShouldReturnThreeThemeForQBelow() = runTest {
        whenever(versionCheckerRepository.isAndroidQAndAbove()).thenReturn(false)

        val themes = repository.getThemes()
        Truth.assertThat(themes).isNotNull()
        Truth.assertThat(themes).isNotEmpty()
        Truth.assertThat(themes).hasSize(3)
    }

    @Test
    fun checkApplyTheme() = runTest {
        repository.applyTheme()
    }
}
