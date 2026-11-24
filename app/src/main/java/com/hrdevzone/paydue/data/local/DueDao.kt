package com.hrdevzone.paydue.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DueDao {

    @Query("SELECT * FROM dues ORDER BY isPaid ASC, dueDateEpochDay ASC")
    fun observeAll(): Flow<List<Due>>

    @Query("SELECT * FROM dues WHERE id = :id")
    suspend fun getById(id: Long): Due?

    @Query("SELECT * FROM dues WHERE id = :id")
    fun observeById(id: Long): Flow<Due?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(due: Due): Long

    @Update
    suspend fun update(due: Due)

    @Delete
    suspend fun delete(due: Due)

    @Query("DELETE FROM dues WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM dues")
    suspend fun count(): Int

    @Query("DELETE FROM dues")
    suspend fun clearAll()

    @Query("SELECT * FROM dues")
    suspend fun getAllOnce(): List<Due>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dues: List<Due>)
}
