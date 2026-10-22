package com.naveenapps.expensemanager.demo

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.repository.DemoDataRepository
import com.naveenapps.expensemanager.initializer.KoinInitializer
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/** DEBUG BUILDS ONLY — backs Settings → Developer → "Load demo data". */
class DemoDataRepositoryImpl : DemoDataRepository {
    override suspend fun loadDemoData(months: Int): Result<Int> = runCatching {
        DemoDataSeeder().seed(currencyCode = null, language = null, months = months).transactions
    }.onFailure { Log.e("DemoData", "Demo data failed", it) }
}

val DemoDataModule = module {
    single<DemoDataRepository> { DemoDataRepositoryImpl() }
}

/**
 * Adds [DemoDataModule] to Koin at app start. Registered only in app/src/debug/AndroidManifest.xml,
 * so release builds never bind DemoDataRepository and Settings hides the option.
 */
class DemoDataInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        loadKoinModules(DemoDataModule)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf(KoinInitializer::class.java)
}
