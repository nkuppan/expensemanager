package com.naveenapps.expensemanager.initializer

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.GetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.SetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

val BASE_CATEGORY_LIST = listOf(
    Category(
        id = "1",
        name = "Clothing",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#F44336",
        iconName = "apparel",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "2",
        name = "Entertainment",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#E91E63",
        iconName = "sports_esports",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "3",
        name = "Food",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#9C27B0",
        iconName = "restaurant",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "4",
        name = "Health",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#673AB7",
        iconName = "home_health",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "5",
        name = "Leisure",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#3F51B5",
        iconName = "flights_and_hotels",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "6",
        name = "Shopping",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#2196F3",
        iconName = "shopping_cart",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "7",
        name = "Transportation",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#03A9F4",
        iconName = "travel",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "8",
        name = "Utilities",
        type = CategoryType.EXPENSE,
        iconBackgroundColor = "#00BCD4",
        iconName = "other_admission",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "9",
        name = "Savings",
        type = CategoryType.INCOME,
        iconBackgroundColor = "#4CAF50",
        iconName = "savings",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "10",
        name = "Bank",
        type = CategoryType.INCOME,
        iconBackgroundColor = "#8BC34A",
        iconName = "account_balance",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "11",
        name = "Credit Card",
        type = CategoryType.INCOME,
        iconBackgroundColor = "#CDDC39",
        iconName = "credit_card",
        createdOn = Date(),
        updatedOn = Date(),
    ),
    Category(
        id = "12",
        name = "Gift",
        type = CategoryType.INCOME,
        iconBackgroundColor = "#FFEB3B",
        iconName = "featured_seasonal_and_gifts",
        createdOn = Date(),
        updatedOn = Date(),
    )
)

val BASE_ACCOUNT_LIST = listOf(
    Account(
        "1",
        "Cash",
        AccountType.REGULAR,
        "#4CAF50",
        "savings",
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "2",
        "Card-xxx",
        AccountType.CREDIT,
        "#4CAF50",
        "credit_card",
        Calendar.getInstance().time,
        Calendar.getInstance().time,
    ),
    Account(
        "3",
        "Bank Account",
        AccountType.REGULAR,
        "#4CAF50",
        "account_balance",
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

        CoroutineScope(Dispatchers.Main).launch {

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