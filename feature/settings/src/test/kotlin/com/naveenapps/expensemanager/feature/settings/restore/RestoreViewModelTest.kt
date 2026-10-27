package com.naveenapps.expensemanager.feature.settings.restore

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.AutoBackupRepository
import com.naveenapps.expensemanager.core.repository.BackupException
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.core.testing.BaseCoroutineTest
import com.naveenapps.expensemanager.feature.settings.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RestoreViewModelTest : BaseCoroutineTest() {

    private val autoBackupRepository: AutoBackupRepository = mock()
    private val backupRepository: BackupRepository = mock()
    private val navigator: AppComposeNavigator = mock()
    private val analytics: AnalyticsRepository = mock()

    private val backup = BackupFile(
        uri = "content://tree/b/document/1",
        name = "expense-manager-auto-2026-10-27-100100.sqlite3.aes",
        createdAt = 1_000L,
        sizeBytes = 1_500_000L,
        kind = BackupKind.AUTO,
        isInsideApp = false,
    )

    private suspend fun viewModel(): RestoreViewModel {
        whenever(autoBackupRepository.getSettings()).thenReturn(flowOf(AutoBackupSettings()))
        whenever(autoBackupRepository.getBackups()).thenReturn(listOf(backup))
        return RestoreViewModel(autoBackupRepository, backupRepository, navigator, analytics)
    }

    @Test
    fun `lists the backups on this device`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        assertThat(viewModel.state.value.backups).containsExactly(backup)
    }

    @Test
    fun `restoring from the list asks first, then restarts`() = runTest {
        whenever(backupRepository.restoreData(backup.uri, null)).thenReturn(Resource.Success(true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(RestoreAction.SelectBackup(backup))
        assertThat(viewModel.state.value.confirmBackup).isEqualTo(backup)
        verify(backupRepository, never()).restoreData(any(), anyOrNull())

        viewModel.event.test {
            viewModel.processAction(RestoreAction.ConfirmRestore)
            assertThat(awaitItem()).isEqualTo(RestoreEvent.RestartApp)
        }
        assertThat(viewModel.state.value.confirmBackup).isNull()
    }

    @Test
    fun `dismissing the confirmation restores nothing`() = runTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(RestoreAction.SelectBackup(backup))
        viewModel.processAction(RestoreAction.DismissConfirmation)
        advanceUntilIdle()

        verify(backupRepository, never()).restoreData(any(), anyOrNull())
    }

    @Test
    fun `a chosen file opens the picker and restores what was picked`() = runTest {
        whenever(backupRepository.restoreData("content://picked", null)).thenReturn(Resource.Success(true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(RestoreAction.ChooseFile)
            assertThat(awaitItem()).isEqualTo(RestoreEvent.PickFile)

            viewModel.processAction(RestoreAction.FileSelected("content://picked"))
            assertThat(awaitItem()).isEqualTo(RestoreEvent.RestartApp)
        }
    }

    @Test
    fun `a protected backup asks for the passphrase and retries the same file`() = runTest {
        whenever(backupRepository.restoreData("content://picked", null))
            .thenReturn(Resource.Error(BackupException(BackupException.Reason.PASSPHRASE_REQUIRED)))
        whenever(backupRepository.restoreData("content://picked", "secret")).thenReturn(Resource.Success(true))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.processAction(RestoreAction.FileSelected("content://picked"))
        advanceUntilIdle()
        assertThat(viewModel.state.value.showPassphrasePrompt).isTrue()

        viewModel.event.test {
            viewModel.processAction(RestoreAction.SubmitPassphrase("secret"))
            assertThat(awaitItem()).isEqualTo(RestoreEvent.RestartApp)
        }
    }

    @Test
    fun `an invalid file shows an error`() = runTest {
        whenever(backupRepository.restoreData("content://picked", null))
            .thenReturn(Resource.Error(BackupException(BackupException.Reason.INVALID_FILE)))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(RestoreAction.FileSelected("content://picked"))
            assertThat(awaitItem()).isEqualTo(RestoreEvent.ShowMessage(R.string.restore_invalid_file))
        }
        assertThat(viewModel.state.value.isRestoring).isFalse()
    }
}
