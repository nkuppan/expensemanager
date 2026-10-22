package com.naveenapps.expensemanager.feature.settings.autobackup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupScheduler
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.feature.settings.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class AutoBackupViewModelTest : BaseCoroutineTest() {

    private val scheduler: AutoBackupScheduler = mock()
    private val navigator: AppComposeNavigator = mock()
    private val analytics: AnalyticsRepository = mock()

    /** In-memory stand-in so state changes flow back to the ViewModel like the real store. */
    private class FakeAutoBackupRepository(initial: AutoBackupSettings) : AutoBackupRepository {
        val settings = MutableStateFlow(initial)
        var folderGranted = true
        var backupResult = AutoBackupResult.SUCCESS
        var backupCalls = 0

        override fun getSettings(): Flow<AutoBackupSettings> = settings
        override suspend fun setEnabled(enabled: Boolean) = settings.update { it.copy(isEnabled = enabled) }
        override suspend fun setFrequency(frequency: AutoBackupFrequency) = settings.update { it.copy(frequency = frequency) }
        override suspend fun setFolder(treeUri: String): Boolean {
            if (folderGranted) settings.update { it.copy(folderUri = treeUri, folderName = "Backups") }
            return folderGranted
        }
        override suspend fun backupNow(): AutoBackupResult {
            backupCalls++
            return backupResult
        }
        override suspend fun snoozeFailureWarning(untilMillis: Long) = settings.update { it.copy(warningSnoozedUntil = untilMillis) }
        override suspend fun markFailureNotified() = settings.update { it.copy(isFailureNotified = true) }
    }

    private fun viewModel(repository: AutoBackupRepository) = AutoBackupViewModel(
        autoBackupRepository = repository,
        autoBackupScheduler = scheduler,
        appComposeNavigator = navigator,
        analyticsRepository = analytics,
    )

    @Test
    fun `turning on without a folder asks for one and enables once picked`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.ToggleEnabled)
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.PickFolder)
        }
        assertThat(repository.settings.value.isEnabled).isFalse()

        viewModel.processAction(AutoBackupAction.FolderSelected("content://tree/backups"))
        advanceUntilIdle()

        assertThat(repository.settings.value.isEnabled).isTrue()
        assertThat(repository.settings.value.folderUri).isEqualTo("content://tree/backups")
        verify(scheduler).schedule(AutoBackupFrequency.DAILY, replaceExisting = true)
    }

    @Test
    fun `backing out of the folder picker leaves it off`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.ToggleEnabled)
        viewModel.processAction(AutoBackupAction.FolderSelected(null))
        advanceUntilIdle()

        assertThat(repository.settings.value.isEnabled).isFalse()
        verify(scheduler, never()).schedule(any(), any())
    }

    @Test
    fun `changing the folder alone does not turn it on`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.ChooseFolder)
        viewModel.processAction(AutoBackupAction.FolderSelected("content://tree/backups"))
        advanceUntilIdle()

        assertThat(repository.settings.value.folderUri).isEqualTo("content://tree/backups")
        assertThat(repository.settings.value.isEnabled).isFalse()
    }

    @Test
    fun `a folder without lasting access shows an error`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings()).apply { folderGranted = false }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.FolderSelected("content://tree/locked"))
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShowMessage(R.string.auto_backup_folder_error))
        }
        assertThat(repository.settings.value.isEnabled).isFalse()
    }

    @Test
    fun `turning off cancels the schedule`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(isEnabled = true, folderUri = "content://tree/b"))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.ToggleEnabled)
        advanceUntilIdle()

        assertThat(repository.settings.value.isEnabled).isFalse()
        verify(scheduler).cancel()
    }

    @Test
    fun `changing frequency reschedules only while on`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(isEnabled = true, folderUri = "content://tree/b"))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.SelectFrequency(AutoBackupFrequency.WEEKLY))
        advanceUntilIdle()

        assertThat(repository.settings.value.frequency).isEqualTo(AutoBackupFrequency.WEEKLY)
        verify(scheduler).schedule(AutoBackupFrequency.WEEKLY, replaceExisting = true)
    }

    @Test
    fun `back up now reports the result`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(folderUri = "content://tree/b"))
            .apply { backupResult = AutoBackupResult.FOLDER_UNAVAILABLE }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.BackupNow)
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShowMessage(R.string.auto_backup_folder_unavailable))
        }
        assertThat(repository.backupCalls).isEqualTo(1)
        assertThat(viewModel.state.value.isBackingUp).isFalse()
    }

    @Test
    fun `back up now does nothing without a folder`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.BackupNow)
        advanceUntilIdle()

        assertThat(repository.backupCalls).isEqualTo(0)
    }
}
