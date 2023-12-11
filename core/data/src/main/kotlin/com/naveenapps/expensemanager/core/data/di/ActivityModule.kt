package com.naveenapps.expensemanager.core.data.di

import android.content.Context
import com.naveenapps.expensemanager.core.data.repository.BackupRepositoryImpl
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.repository.BackupRepository
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
}