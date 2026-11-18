package com.naveenapps.expensemanager.feature.about.licenses

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OpenSourceLicenseParserTest {

    private val apacheUrl = "http://www.apache.org/licenses/LICENSE-2.0.txt"
    private val mitText = "MIT License\n\nPermission is hereby granted, free of charge, to any person…"
    private val licenses = (apacheUrl + mitText).toByteArray()
    private val mitOffset = apacheUrl.toByteArray().size
    private val mitLength = mitText.toByteArray().size

    @Test
    fun `reads every library sorted by name and drops duplicates`() {
        val metadata = """
            0:${apacheUrl.length} Room
            $mitOffset:$mitLength kotlinx-serialization-json
            0:${apacheUrl.length} Activity Compose
            0:${apacheUrl.length} room
        """.trimIndent()

        val result = OpenSourceLicenseParser.parse(metadata, licenses)

        assertThat(result.map { it.name }).containsExactly("Activity Compose", "kotlinx-serialization-json", "Room")
            .inOrder()
        assertThat(result.first().url).isEqualTo(apacheUrl)
        assertThat(result.first().kind).isEqualTo(LicenseKind.APACHE_2)
        assertThat(result[1].url).isNull()
        assertThat(result[1].kind).isEqualTo(LicenseKind.MIT)
    }

    @Test
    fun `broken lines are skipped instead of crashing`() {
        val metadata = """
            not a line
            5 missing length
            0:99999 Out of range
            0:${apacheUrl.length}
            0:${apacheUrl.length} Valid
        """.trimIndent()

        assertThat(OpenSourceLicenseParser.parse(metadata, licenses).map { it.name }).containsExactly("Valid")
    }

    @Test
    fun `offsets count UTF-8 bytes, not characters`() {
        val first = "Licença ©"
        val second = "https://example.com/license"
        val bytes = (first + second).toByteArray()
        val metadata = "${first.toByteArray().size}:${second.length} Library"

        assertThat(OpenSourceLicenseParser.parse(metadata, bytes).single().text).isEqualTo(second)
    }

    @Test
    fun `common licences are recognised from their text or link`() {
        assertThat(LicenseKind.of("https://opensource.org/licenses/BSD-3-Clause")).isEqualTo(LicenseKind.BSD)
        assertThat(LicenseKind.of("https://developer.android.com/studio/terms.html"))
            .isEqualTo(LicenseKind.ANDROID_SDK)
        assertThat(LicenseKind.of("Some custom terms")).isEqualTo(LicenseKind.OTHER)
    }
}
