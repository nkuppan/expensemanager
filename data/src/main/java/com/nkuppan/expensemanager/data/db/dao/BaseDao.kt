package com.nkuppan.expensemanager.data.db.dao

import androidx.room.*


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