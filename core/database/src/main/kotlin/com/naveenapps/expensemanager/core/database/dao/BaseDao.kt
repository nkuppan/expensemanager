package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: T): Long

    @Update
    suspend fun update(value: T)

    @Update
    suspend fun updateAll(value: List<T>)

    @Delete
    suspend fun delete(value: T): Int
}
