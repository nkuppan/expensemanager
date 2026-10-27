package com.naveenapps.expensemanager.core.data.repository.backup

import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import org.junit.Test

class AutoBackupFilesTest {

    private fun date(y: Int, m: Int, d: Int, h: Int, min: Int, s: Int) = Date.from(LocalDateTime.of(y, m, d, h, min, s).atZone(ZoneId.systemDefault()).toInstant())

    private fun auto(stamp: String) = "expense-manager-auto-$stamp.sqlite3.aes"

    @Test
    fun `file name has a sortable timestamp`() {
        assertThat(AutoBackupFiles.fileName(date(2026, 10, 3, 7, 5, 9)))
            .isEqualTo("expense-manager-auto-2026-10-03-070509.sqlite3.aes")
    }

    @Test
    fun `recognises automatic backups even when a provider renamed them`() {
        assertThat(AutoBackupFiles.isAutoBackup(auto("2026-10-03-070509"))).isTrue()
        assertThat(AutoBackupFiles.isAutoBackup("expense-manager-auto-2026-10-03-070509 (1).sqlite3.aes")).isTrue()
        assertThat(AutoBackupFiles.isAutoBackup("expense-manager-auto-2026-10-03-070509.sqlite3.aes.bin")).isTrue()
    }

    @Test
    fun `never treats manual backups or other files as automatic`() {
        assertThat(AutoBackupFiles.isAutoBackup("expense-manager-2026-10-03-070509.sqlite3.aes")).isFalse()
        assertThat(AutoBackupFiles.isAutoBackup("expense-manager-auto-notes.txt")).isFalse()
        assertThat(AutoBackupFiles.isAutoBackup("holiday.jpg")).isFalse()
    }

    @Test
    fun `keeps the newest and deletes the rest, oldest last`() {
        val names = listOf(
            auto("2026-10-01-090000"),
            auto("2026-10-03-090000"),
            "expense-manager-2026-09-01-090000.sqlite3.aes", // manual
            auto("2026-09-28-090000"),
            auto("2026-10-02-090000"),
            "receipt.pdf",
        )

        val toDelete = AutoBackupFiles.toDelete(names, keep = 2)

        assertThat(toDelete).containsExactly(auto("2026-10-01-090000"), auto("2026-09-28-090000")).inOrder()
    }

    @Test
    fun `nothing is deleted while under the limit`() {
        val names = listOf(auto("2026-10-01-090000"), auto("2026-10-02-090000"))

        assertThat(AutoBackupFiles.toDelete(names, keep = 7)).isEmpty()
    }

    @Test
    fun `manual backups use the backup file name`() {
        assertThat(AutoBackupFiles.manualFileName(date(2026, 10, 3, 7, 5, 9)))
            .isEqualTo("expense-manager-2026-10-03-070509.sqlite3.aes")
    }

    @Test
    fun `parses kind and time from both names`() {
        val time = date(2026, 10, 3, 7, 5, 9).time

        assertThat(AutoBackupFiles.parse(auto("2026-10-03-070509")))
            .isEqualTo(AutoBackupFiles.Parsed(isAuto = true, createdAt = time))
        assertThat(AutoBackupFiles.parse("expense-manager-2026-10-03-070509 (1).sqlite3.aes"))
            .isEqualTo(AutoBackupFiles.Parsed(isAuto = false, createdAt = time))
    }

    @Test
    fun `ignores files the app didn't write`() {
        assertThat(AutoBackupFiles.parse("holiday.jpg")).isNull()
        assertThat(AutoBackupFiles.parse("expense-manager-notes.txt")).isNull()
        assertThat(AutoBackupFiles.parse("expense-manager-2026-13-45-990000.sqlite3.aes")).isNull()
    }
}
