package com.naveenapps.expensemanager.feature.settings.autobackup

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupResult
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.BackupFile
import com.naveenapps.expensemanager.core.model.BackupKind
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class AutoBackupViewModelTest : BaseCoroutineTest() {

    private val scheduler: AutoBackupScheduler = mock()
    private val navigator: AppComposeNavigator = mock()
    private val analytics: AnalyticsRepository = mock()

    /** In-memory stand-in so state changes flow back to the ViewModel like the real store. */
    private class FakeAutoBackupRepository(initial: AutoBackupSettings) : AutoBackupRepository {
        val settings = MutableStateFlow(initial)
        val backups = mutableListOf<BackupFile>()
        var folderGranted = true
        var backupResult = AutoBackupResult.SUCCESS
        val backupKinds = mutableListOf<BackupKind>()
        var shareUri: String? = "content://share/1"
        var sharedWith: Pair<BackupFile, String?>? = null

        override fun getSettings(): Flow<AutoBackupSettings> = settings
        override suspend fun setEnabled(enabled: Boolean) = settings.update { it.copy(isEnabled = enabled) }
        override suspend fun setFrequency(frequency: AutoBackupFrequency) = settings.update { it.copy(frequency = frequency) }
        override suspend fun setFolder(treeUri: String): Boolean {
            if (folderGranted) settings.update { it.copy(folderUri = treeUri, folderName = "Backups") }
            return folderGranted
        }
        override suspend fun backupNow(kind: BackupKind): AutoBackupResult {
            backupKinds += kind
            if (backupResult == AutoBackupResult.SUCCESS) {
                backups.add(0, backup("new", kind))
                settings.update { it.copy(lastBackupAt = 2_000L) }
            }
            return backupResult
        }
        override suspend fun getBackups(): List<BackupFile> = backups.toList()
        override suspend fun prepareShare(backup: BackupFile, passphrase: String?): String? {
            sharedWith = backup to passphrase
            return shareUri
        }
        override suspend fun snoozeFailureWarning(untilMillis: Long) = settings.update { it.copy(warningSnoozedUntil = untilMillis) }
        override suspend fun markFailureNotified() = settings.update { it.copy(isFailureNotified = true) }
    }

    private companion object {
        fun backup(name: String, kind: BackupKind = BackupKind.MANUAL) = BackupFile(
            uri = "file:///backups/$name",
            name = name,
            createdAt = 1_000L,
            sizeBytes = 1_500_000L,
            kind = kind,
            isInsideApp = true,
        )
    }

    private fun viewModel(repository: AutoBackupRepository) = AutoBackupViewModel(
        autoBackupRepository = repository,
        autoBackupScheduler = scheduler,
        appComposeNavigator = navigator,
        analyticsRepository = analytics,
    )

    @Test
    fun `lists the backups on open`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings()).apply { backups += backup("a") }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        assertThat(viewModel.state.value.backups.map { it.name }).containsExactly("a")
    }

    @Test
    fun `back up now works without a folder and refreshes the list`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.BackupNow)
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShowMessage(R.string.backup_success))
        }
        advanceUntilIdle()

        assertThat(repository.backupKinds).containsExactly(BackupKind.MANUAL)
        assertThat(viewModel.state.value.backups.map { it.name }).containsExactly("new")
        assertThat(viewModel.state.value.isBackingUp).isFalse()
    }

    @Test
    fun `back up now reports an unreachable folder`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(folderUri = "content://tree/b"))
            .apply { backupResult = AutoBackupResult.FOLDER_UNAVAILABLE }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.BackupNow)
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShowMessage(R.string.auto_backup_folder_unavailable))
        }
    }

    @Test
    fun `turning on needs no folder and schedules right away`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(frequency = AutoBackupFrequency.MONTHLY))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.ToggleEnabled)
        advanceUntilIdle()

        assertThat(repository.settings.value.isEnabled).isTrue()
        verify(scheduler).schedule(AutoBackupFrequency.MONTHLY, replaceExisting = true)
    }

    @Test
    fun `turning off cancels the schedule`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(isEnabled = true))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.ToggleEnabled)
        advanceUntilIdle()

        assertThat(repository.settings.value.isEnabled).isFalse()
        verify(scheduler).cancel()
    }

    @Test
    fun `changing frequency reschedules while on`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings(isEnabled = true))
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.processAction(AutoBackupAction.SelectFrequency(AutoBackupFrequency.WEEKLY))
        advanceUntilIdle()

        assertThat(repository.settings.value.frequency).isEqualTo(AutoBackupFrequency.WEEKLY)
        verify(scheduler).schedule(AutoBackupFrequency.WEEKLY, replaceExisting = true)
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
        assertThat(repository.settings.value.folderUri).isNull()
    }

    @Test
    fun `sharing asks for an optional passphrase then opens the share sheet`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings())
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val file = backup("a")

        viewModel.processAction(AutoBackupAction.Share(file))
        assertThat(viewModel.state.value.shareCandidate).isEqualTo(file)

        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.SubmitSharePassphrase(""))
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShareFile("content://share/1"))
        }
        assertThat(repository.sharedWith).isEqualTo(file to null)
        assertThat(viewModel.state.value.shareCandidate).isNull()
    }

    @Test
    fun `sharing with a passphrase passes it on and reports failures`() = runTest {
        val repository = FakeAutoBackupRepository(AutoBackupSettings()).apply { shareUri = null }
        val viewModel = viewModel(repository)
        advanceUntilIdle()
        val file = backup("a")

        viewModel.processAction(AutoBackupAction.Share(file))
        viewModel.event.test {
            viewModel.processAction(AutoBackupAction.SubmitSharePassphrase("secret"))
            assertThat(awaitItem()).isEqualTo(AutoBackupEvent.ShowMessage(R.string.share_backup_failed))
        }
        assertThat(repository.sharedWith).isEqualTo(file to "secret")
    }
}
