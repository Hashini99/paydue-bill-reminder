package com.hrdevzone.paydue.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentEventDao {

    @Insert
    suspend fun insert(event: PaymentEvent)

    @Query("SELECT * FROM payment_events WHERE paidEpochDay >= :sinceEpochDay ORDER BY paidEpochDay ASC")
    fun observeSince(sinceEpochDay: Long): Flow<List<PaymentEvent>>

    @Query("DELETE FROM payment_events")
    suspend fun clearAll()
}
