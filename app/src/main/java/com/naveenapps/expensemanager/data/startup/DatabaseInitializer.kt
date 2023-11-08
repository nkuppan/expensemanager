package com.naveenapps.expensemanager.data.startup

import android.content.Context
import androidx.startup.Initializer
import com.naveenapps.expensemanager.domain.usecase.account.GetAllAccountsUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DatabaseInitializer : Initializer<Unit> {

    @Inject
    lateinit var getAllAccountUseCase: GetAllAccountsUseCase

    @OptIn(DelicateCoroutinesApi::class)
    override fun create(context: Context) {

        InitializerEntryPoint.resolve(context).inject(this)

        GlobalScope.launch {
            getAllAccountUseCase.invoke()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(DependencyGraphInitializer::class.java)
    }
}