package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The insight history moved from org.json to kotlinx.serialization (OpenSpec change
 * make-core-datastore-multiplatform, design D2). Histories already on users' phones were written
 * by the old code, and a downgrade would read the new code's output with the old code, so both
 * directions must give the same entries. The old code is copied below as the reference.
 */
class InsightJsonCompatibilityTest {

    private val insightsKey = stringPreferencesKey("insight_history")
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val folder: File = Files.createTempDirectory("insights").toFile()
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope) {
        File(folder, "test.preferences_pb")
    }
    private val store = FeedbackDataStore(dataStore)

    @After
    fun tearDown() {
        scope.cancel()
        folder.deleteRecursively()
    }

    // region the previous org.json code, verbatim apart from working on a String

    private fun oldAdd(stored: String?, kind: String, title: String, message: String, postedAt: Long): String {
        val history = runCatching { JSONArray(stored ?: "[]") }.getOrDefault(JSONArray())
        val updated = JSONArray().put(
            JSONObject()
                .put("kind", kind)
                .put("title", title)
                .put("message", message)
                .put("postedAt", postedAt),
        )
        for (i in 0 until minOf(history.length(), MAX_INSIGHTS - 1)) updated.put(history.get(i))
        return updated.toString()
    }

    private fun oldRead(stored: String?): List<StoredInsight> {
        val history = runCatching { JSONArray(stored ?: "[]") }.getOrDefault(JSONArray())
        return (0 until history.length()).mapNotNull { i ->
            runCatching {
                val item = history.getJSONObject(i)
                StoredInsight(
                    kind = item.optString("kind"),
                    title = item.getString("title"),
                    message = item.getString("message"),
                    postedAt = item.getLong("postedAt"),
                )
            }.getOrNull()
        }
    }

    // endregion

    private fun stored(): String? = runBlocking { dataStore.data.first()[insightsKey] }

    private fun storeRaw(value: String) = runBlocking { dataStore.edit { it[insightsKey] = value } }

    private val samples = listOf(
        StoredInsight("weekly_summary", "Your week", "You spent ₹1,240 — 12% less than last week.", 1_795_000_000_000),
        StoredInsight("budget_alert", "Groceries at 85%", "\"Quotes\", back\\slash, </tags> and a\nnew line", 1_795_100_000_000),
        StoredInsight("", "No kind", "Emoji 🎉 and 中文", 1_795_200_000_000),
    )

    @Test
    fun historyWrittenByTheOldCodeIsReadTheSame() {
        var old: String? = null
        samples.forEach { old = oldAdd(old, it.kind, it.title, it.message, it.postedAt) }
        // An entry that isn't an object and one without a title: both skipped by either reader.
        val withBroken = JSONArray(old).put(1, "not an object").put(JSONObject().put("message", "no title").put("postedAt", 1))
        storeRaw(withBroken.toString())

        assertEquals(oldRead(withBroken.toString()), runBlocking { store.getInsights().first() })
        assertEquals(samples.reversed().take(1) + samples.reversed().drop(2), oldRead(withBroken.toString()))
    }

    @Test
    fun historyWrittenByTheNewCodeIsReadTheSameByTheOldCode() {
        runBlocking { samples.forEach { store.addInsight(it.kind, it.title, it.message, it.postedAt) } }

        assertEquals(samples.reversed(), oldRead(stored()))
        assertEquals(samples.reversed(), runBlocking { store.getInsights().first() })
    }

    @Test
    fun newEntriesKeepOldEntriesTheyCannotReadAndTheLimitHolds() {
        storeRaw(JSONArray().put("garbage").put(JSONObject().put("title", "t").put("message", "m").put("postedAt", 5)).toString())
        runBlocking { (1..60).forEach { store.addInsight("k", "title $it", "message $it", it.toLong()) } }

        val newRead = runBlocking { store.getInsights().first() }
        assertEquals(MAX_INSIGHTS, JSONArray(stored()).length())
        assertEquals((60 downTo 11).map { StoredInsight("k", "title $it", "message $it", it.toLong()) }, newRead)
        assertEquals(newRead, oldRead(stored()))

        // With fewer than the limit, an unreadable old entry is copied, not dropped (as before).
        storeRaw(JSONArray().put("garbage").toString())
        runBlocking { store.addInsight("k", "t", "m", 1) }
        assertEquals(oldAdd(JSONArray().put("garbage").toString(), "k", "t", "m", 1).let { JSONArray(it).length() }, JSONArray(stored()).length())
        assertEquals("garbage", JSONArray(stored()).get(1))
    }

    @Test
    fun aBrokenOrMissingValueIsAnEmptyHistory() {
        assertEquals(emptyList<StoredInsight>(), runBlocking { store.getInsights().first() })
        storeRaw("{not json")
        assertEquals(emptyList<StoredInsight>(), runBlocking { store.getInsights().first() })
        runBlocking { store.addInsight("k", "t", "m", 1) }
        assertEquals(listOf(StoredInsight("k", "t", "m", 1)), oldRead(stored()))
    }
}
