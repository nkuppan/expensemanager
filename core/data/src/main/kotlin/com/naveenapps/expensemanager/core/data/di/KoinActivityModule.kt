package com.naveenapps.expensemanager.core.data.di

import android.app.Activity
import com.naveenapps.expensemanager.core.data.repository.ShareRepositoryImpl
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.core.repository.ShareRepository
import org.koin.androidx.scope.dsl.activityScope
import org.koin.dsl.module

val ActivityModule = module {
    activityScope {
        scoped<ShareRepository> {
            ShareRepositoryImpl(
                context = get<Activity>(),
                firebaseSettingsRepository = get(),
            )
        }
        scoped<ActivityComponentProvider> {
            val shareRepository: ShareRepository = get()

            return@scoped object : ActivityComponentProvider {
                override fun getShareRepository() = shareRepository
            }
        }
    }
}
