package com.naveenapps.expensemanager.core.data.repository.backup

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.needsAttention
import org.junit.Test

/** When Home shows the "automatic backup isn't working" banner. */
class AutoBackupWarningTest {

    private val now = 1_800_000_000_000L
    private val failing = AutoBackupSettings(isEnabled = true, folderUri = "content://tree/b", lastError = AutoBackupError.FOLDER_UNAVAILABLE)

    @Test
    fun `shown while backups are on and failing`() {
        assertThat(failing.needsAttention(now)).isTrue()
    }

    @Test
    fun `hidden when the last backup worked`() {
        assertThat(failing.copy(lastError = null).needsAttention(now)).isFalse()
    }

    @Test
    fun `hidden when automatic backup is off`() {
        assertThat(failing.copy(isEnabled = false).needsAttention(now)).isFalse()
    }

    @Test
    fun `hidden while snoozed and back once the snooze ends`() {
        val snoozed = failing.copy(warningSnoozedUntil = now + 1)

        assertThat(snoozed.needsAttention(now)).isFalse()
        assertThat(snoozed.needsAttention(now + 1)).isTrue()
    }
}
