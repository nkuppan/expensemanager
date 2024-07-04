package com.naveenapps.expensemanager.core.data.di

import android.content.Context
import com.naveenapps.expensemanager.core.data.repository.BackupRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ShareRepositoryImpl
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.core.repository.FirebaseSettingsRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

@Module
@InstallIn(ActivityComponent::class)
object ActivityModule {

    @Provides
    fun provideRoomBackup(@ActivityContext context: Context): RoomBackup {
        return RoomBackup(context)
    }

    @Provides
    fun provideBackupRepository(
        roomBackup: RoomBackup,
        expenseManagerDatabase: ExpenseManagerDatabase
    ): BackupRepository {
        return BackupRepositoryImpl(roomBackup, expenseManagerDatabase)
    }

    @Provides
    fun providesShareRepository(
        @ActivityContext context: Context,
        firebaseSettingsRepository: FirebaseSettingsRepository,
    ): ShareRepository {
        return ShareRepositoryImpl(
            context,
            firebaseSettingsRepository
        )
    }

    @Provides
    fun provideActivityComponentProvider(
        backupRepository: BackupRepository,
        shareRepository: ShareRepository,
    ): ActivityComponentProvider {
        return object : ActivityComponentProvider {
            override fun getBackupRepository(): BackupRepository {
                return backupRepository
            }

            override fun getShareRepository(): ShareRepository {
                return shareRepository
            }
        }
    }
}