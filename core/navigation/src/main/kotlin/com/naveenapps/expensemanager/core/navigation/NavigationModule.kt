package com.naveenapps.expensemanager.core.navigation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NavigationModule {

    @Binds
    @Singleton
    abstract fun provideComposeNavigator(
        expenseManagerComposeNavigator: ExpenseManagerComposeNavigator,
    ): AppComposeNavigator
}
