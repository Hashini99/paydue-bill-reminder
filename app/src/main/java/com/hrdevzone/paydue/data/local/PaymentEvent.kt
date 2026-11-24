package com.hrdevzone.paydue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hrdevzone.paydue.data.model.DueCategory
import com.hrdevzone.paydue.data.model.DueDirection

@Entity(tableName = "payment_events")
data class PaymentEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dueId: Long,
    val title: String,
    val category: DueCategory,
    val direction: DueDirection,
    val amount: Double,
    val paidEpochDay: Long
)
