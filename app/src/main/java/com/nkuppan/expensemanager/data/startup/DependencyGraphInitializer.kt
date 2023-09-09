package com.nkuppan.expensemanager.data.startup

import android.content.Context
import androidx.startup.Initializer

class DependencyGraphInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        //this will lazily initialize ApplicationComponent before Application's `onCreate`
        InitializerEntryPoint.resolve(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}