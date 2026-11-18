package com.naveenapps.expensemanager.feature.dashboard.notifications

import androidx.annotation.ArrayRes
import com.naveenapps.expensemanager.feature.dashboard.R
import java.time.LocalDate

/** One release's highlights: the version, when it came out, and its items in every language. */
data class WhatsNewRelease(
    val version: String,
    /** Places the notes among the insights in the notification centre. */
    val releasedOn: LocalDate,
    @ArrayRes val items: Int,
)

/**
 * Every release's "What's new", newest first; the notification centre shows them all.
 *
 * For a new release with notes: add a `whats_new_<version>` string-array (all 16 languages,
 * usually the same lines as `app/src/main/play/release-notes`) and put it at the top here. The
 * bell then shows a dot again for everyone who updated. A release without notes adds nothing,
 * so nobody gets a dot for nothing.
 */
val WHATS_NEW_RELEASES: List<WhatsNewRelease> = listOf(
    WhatsNewRelease(version = "1.4.30", releasedOn = LocalDate.of(2026, 11, 13), items = R.array.whats_new_1_4_30),
    WhatsNewRelease(version = "1.4.29", releasedOn = LocalDate.of(2026, 11, 12), items = R.array.whats_new_1_4_29),
    WhatsNewRelease(version = "1.4.28", releasedOn = LocalDate.of(2026, 11, 8), items = R.array.whats_new_1_4_28),
    WhatsNewRelease(version = "1.4.26", releasedOn = LocalDate.of(2026, 11, 7), items = R.array.whats_new_1_4_26),
)

/** The newest notes; the bell's dot shows until this version has been opened. */
val LATEST_WHATS_NEW_VERSION: String = WHATS_NEW_RELEASES.first().version
