package com.hrdevzone.paydue.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val invoiceNumber: String,
    val fromName: String,
    val clientName: String,
    val amount: Double,
    val issueDateEpochDay: Long,
    val paymentNote: String = "",
    val note: String = "",
    val linkedDueId: Long? = null,
    val pdfFileName: String,
    val createdAt: Long = System.currentTimeMillis()
)
