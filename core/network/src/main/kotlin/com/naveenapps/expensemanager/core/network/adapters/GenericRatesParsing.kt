package com.naveenapps.expensemanager.core.network.adapters

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Shared by every adapter whose response already carries `{ "rates": { "CODE": number } }`. */
internal fun parseGenericRatesObject(body: JsonObject): Map<String, Double> {
    val ratesNode = body["rates"]?.jsonObject ?: return emptyMap()
    return ratesNode.mapNotNull { (code, value) ->
        val rate = value.jsonPrimitive.doubleOrNull ?: return@mapNotNull null
        code to rate
    }.toMap()
}
