package com.naveenapps.expensemanager.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update


@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(value: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(value: List<T>)

    @Update
    suspend fun update(value: T)

    @Update
    suspend fun updateAll(value: List<T>)

    @Delete
    suspend fun delete(value: T): Int
}