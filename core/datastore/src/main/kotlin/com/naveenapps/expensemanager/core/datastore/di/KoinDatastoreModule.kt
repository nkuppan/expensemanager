import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.datastore.DateRangeDataStore
import com.naveenapps.expensemanager.core.datastore.FeedbackDataStore
import com.naveenapps.expensemanager.core.datastore.ReminderTimeDataStore
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.datastore.ThemeDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val DATA_STORE_NAME = "expense_manager_app_data_store"

private val Context.dataStore by preferencesDataStore(DATA_STORE_NAME)

val DatastoreModule = module {
    single { androidContext().dataStore }
    single { ThemeDataStore(get()) }
    single { CurrencyDataStore(get()) }
    single { ReminderTimeDataStore(get()) }
    single { SettingsDataStore(get()) }
    single { DateRangeDataStore(get()) }
    single { FeedbackDataStore(get()) }
}

