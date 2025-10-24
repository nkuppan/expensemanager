import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val DispatcherModule = module {
    single {
        AppCoroutineDispatchers(
            main = Dispatchers.Main,
            io = Dispatchers.IO,
            computation = Dispatchers.Default
        )
    }
}

