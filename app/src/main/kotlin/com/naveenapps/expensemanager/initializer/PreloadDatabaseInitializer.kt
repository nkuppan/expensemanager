package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

val BASE_CATEGORY_LIST = listOf(
    Category(
        id = "1",
        name = "Clothing",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "apparel",
            backgroundColor = "#F44336",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "2",
        name = "Entertainment",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "sports_esports",
            backgroundColor = "#E91E63",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "3",
        name = "Food",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "restaurant",
            backgroundColor = "#9C27B0",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "4",
        name = "Health",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "home_health",
            backgroundColor = "#673AB7",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "5",
        name = "Leisure",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "flights_and_hotels",
            backgroundColor = "#3F51B5",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "6",
        name = "Shopping",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "shopping_cart",
            backgroundColor = "#2196F3",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "7",
        name = "Transportation",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "travel",
            backgroundColor = "#03A9F4",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "8",
        name = "Utilities",
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(
            name = "other_admission",
            backgroundColor = "#00BCD4",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "9",
        name = "Salary",
        type = CategoryType.INCOME,
        storedIcon = StoredIcon(
            name = "savings",
            backgroundColor = "#4CAF50",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "10",
        name = "Gift",
        type = CategoryType.INCOME,
        storedIcon = StoredIcon(
            name = "featured_seasonal_and_gifts",
            backgroundColor = "#E65100",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "11",
        name = "Coupons",
        type = CategoryType.INCOME,
        storedIcon = StoredIcon(
            name = "redeem",
            backgroundColor = "#3E2723",
        ),
        createdOn = Date(),
        updatedOn = Date(),
    )
)

val BASE_ACCOUNT_LIST = listOf(
    Account(
        "1",
        "Cash",
        AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "savings",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "2",
        "Card-xxx",
        AccountType.CREDIT,
        storedIcon = StoredIcon(
            name = "credit_card",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "3",
        "Bank Account",
        AccountType.REGULAR,
        storedIcon = StoredIcon(
            name = "account_balance",
            backgroundColor = "#4CAF50",
        ),
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    )
)

class PreloadDatabaseInitializer : Initializer<Unit> {

    @Inject
    lateinit var getPreloadStatusUseCase: GetPreloadStatusUseCase

    @Inject
    lateinit var setPreloadStatusUseCase: SetPreloadStatusUseCase

    @Inject
    lateinit var addAccountUseCase: AddAccountUseCase

    @Inject
    lateinit var addCategoryUseCase: AddCategoryUseCase

    override fun create(context: Context) {

        InitializerEntryPoint.resolve(context).inject(this)

        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {

            val isPreloaded = getPreloadStatusUseCase.invoke()

            if (isPreloaded.not()) {

                BASE_CATEGORY_LIST.forEach { value ->
                    addCategoryUseCase.invoke(value)
                }

                BASE_ACCOUNT_LIST.forEach { value ->
                    addAccountUseCase.invoke(value)
                }

                setPreloadStatusUseCase.invoke(true)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }
}