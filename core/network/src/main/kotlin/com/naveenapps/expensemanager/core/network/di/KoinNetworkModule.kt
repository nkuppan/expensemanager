import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.naveenapps.expensemanager.core.network.ExchangeRateApi
import com.naveenapps.expensemanager.core.network.ExchangeRateApiImpl
import com.naveenapps.expensemanager.core.network.ExchangeRateApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

private const val PLACEHOLDER_BASE_URL = "https://expensemanager.invalid/"

val NetworkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }
    single {
        val json = get<Json>()
        Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ExchangeRateApiService::class.java)
    }
    single<ExchangeRateApi> { ExchangeRateApiImpl(get()) }
}
