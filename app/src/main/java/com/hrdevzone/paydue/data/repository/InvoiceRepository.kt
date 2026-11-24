package com.hrdevzone.paydue.data.repository

import com.hrdevzone.paydue.data.local.InvoiceDao
import com.hrdevzone.paydue.data.local.InvoiceRecord
import kotlinx.coroutines.flow.Flow

class InvoiceRepository(
    private val invoiceDao: InvoiceDao
) {
    fun observeAll(): Flow<List<InvoiceRecord>> = invoiceDao.observeAll()

    suspend fun getById(id: Long): InvoiceRecord? = invoiceDao.getById(id)

    suspend fun save(invoice: InvoiceRecord): Long = invoiceDao.insert(invoice)

    suspend fun delete(invoice: InvoiceRecord) = invoiceDao.delete(invoice)

    suspend fun nextInvoiceNumber(): String {
        val count = invoiceDao.count()
        val year = java.time.LocalDate.now().year
        return "INV-$year-${(count + 1).toString().padStart(3, '0')}"
    }
}
