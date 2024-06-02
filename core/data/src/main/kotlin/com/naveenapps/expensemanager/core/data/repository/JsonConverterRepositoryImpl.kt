package com.naveenapps.expensemanager.core.data.repository

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.repository.JsonConverterRepository
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JsonConverterRepositoryImpl @Inject constructor(
    private val gson: Gson,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
) : JsonConverterRepository {

    override suspend fun fromJsonToObject(value: String, classValue: Class<*>): Any? =
        withContext(appCoroutineDispatchers.io) {
            return@withContext runCatching {
                gson.fromJson(Uri.decode(value), classValue)
            }.onFailure {
                Log.d("Countries", it.message ?: "")
            }.getOrNull()
        }

    override suspend fun fromObjectToJson(value: Any): String? =
        withContext(appCoroutineDispatchers.io) {
            return@withContext runCatching {
                Uri.encode(gson.toJson(value))
            }.onFailure {
                Log.d("Countries", it.message ?: "")
            }.getOrNull()
        }
}
