package com.hrdevzone.paydue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hrdevzone.paydue.data.model.DueCategory
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.data.model.RepeatType

@Entity(tableName = "dues")
data class Due(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val amount: Double,
    val dueDateEpochDay: Long,
    val category: DueCategory,
    val notes: String = "",
    val repeatType: RepeatType = RepeatType.ONCE,
    val direction: DueDirection = DueDirection.PAYABLE,
    val personName: String = "",
    val isPaid: Boolean = false,
    val lastPaidEpochDay: Long? = null,
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
