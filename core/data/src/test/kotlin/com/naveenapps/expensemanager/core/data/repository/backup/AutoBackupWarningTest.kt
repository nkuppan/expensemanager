package com.naveenapps.expensemanager.core.data.repository.backup

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AutoBackupError
import com.naveenapps.expensemanager.core.model.AutoBackupFrequency
import com.naveenapps.expensemanager.core.model.AutoBackupSettings
import com.naveenapps.expensemanager.core.model.needsAttention
import com.naveenapps.expensemanager.core.model.nextAutoBackupAt
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

    @Test
    fun `next automatic backup counts from the last one`() {
        val day = 24L * 60 * 60 * 1000
        val weekly = AutoBackupSettings(isEnabled = true, frequency = AutoBackupFrequency.WEEKLY, lastAutoBackupAt = now)

        assertThat(weekly.nextAutoBackupAt(now)).isEqualTo(now + 7 * day)
        assertThat(weekly.copy(frequency = AutoBackupFrequency.MONTHLY).nextAutoBackupAt(now)).isEqualTo(now + 30 * day)
        // Overdue (e.g. postponed by a low battery): due now.
        assertThat(weekly.nextAutoBackupAt(now + 8 * day)).isEqualTo(now + 8 * day)
    }

    @Test
    fun `no next time while off or before the first run`() {
        val settings = AutoBackupSettings(isEnabled = true, lastAutoBackupAt = null)

        assertThat(settings.nextAutoBackupAt(now)).isNull()
        assertThat(settings.copy(isEnabled = false, lastAutoBackupAt = now).nextAutoBackupAt(now)).isNull()
    }
}
