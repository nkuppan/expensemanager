package com.naveenapps.expensemanager.feature.dashboard.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Insight
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** One entry in the notification centre: a release's notes or a pushed insight. */
sealed interface NotificationItem {
    /** Epoch millis; the list is newest first. */
    val time: Long

    /** Arrived since the user last opened the centre. */
    val isNew: Boolean

    data class Release(val release: WhatsNewRelease, override val time: Long, override val isNew: Boolean) : NotificationItem

    data class InsightItem(val insight: Insight, override val isNew: Boolean) : NotificationItem {
        override val time: Long get() = insight.postedAt
    }
}

data class NotificationCentreState(val items: List<NotificationItem> = emptyList())

/**
 * The notification centre (Home's bell): release notes and pushed insights, newest first.
 * Opening it counts as reading everything, so the bell's dot goes off; what arrived since the
 * last visit is still marked "New" while the screen is open.
 */
class NotificationCentreViewModel(
    private val feedbackRepository: FeedbackRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationCentreState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // Read what was already seen before marking everything as read.
            val openedBefore = feedbackRepository.getNotificationCentreOpenedAt().first()
            val seenRelease = feedbackRepository.getLastSeenWhatsNew().first()
            feedbackRepository.setNotificationCentreOpenedAt(System.currentTimeMillis())
            feedbackRepository.setLastSeenWhatsNew(LATEST_WHATS_NEW_VERSION)

            feedbackRepository.getInsights()
                .onEach { insights ->
                    _state.update {
                        it.copy(items = notificationItems(WHATS_NEW_RELEASES, insights, openedBefore, seenRelease))
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}

/**
 * Releases and insights in one list, newest first. A release counts as dated at the start of its
 * day; only the latest release can be "New" (older notes were shown by earlier versions).
 */
internal fun notificationItems(
    releases: List<WhatsNewRelease>,
    insights: List<Insight>,
    openedBefore: Long,
    seenRelease: String?,
    zone: ZoneId = ZoneId.systemDefault(),
): List<NotificationItem> {
    val latest = releases.firstOrNull()?.version
    val releaseItems = releases.map { release ->
        NotificationItem.Release(
            release = release,
            time = release.releasedOn.atStartOfDay(zone).toInstant().toEpochMilli(),
            isNew = release.version == latest && seenRelease != latest,
        )
    }
    val insightItems = insights.map { NotificationItem.InsightItem(it, isNew = it.postedAt > openedBefore) }
    return (releaseItems + insightItems).sortedByDescending { it.time }
}
