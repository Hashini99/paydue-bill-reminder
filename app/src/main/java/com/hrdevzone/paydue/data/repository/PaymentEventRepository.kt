package com.hrdevzone.paydue.data.repository

import com.hrdevzone.paydue.data.local.PaymentEvent
import com.hrdevzone.paydue.data.local.PaymentEventDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PaymentEventRepository(
    private val paymentEventDao: PaymentEventDao
) {
    suspend fun record(event: PaymentEvent) = paymentEventDao.insert(event)

    fun observeLastMonths(months: Int): Flow<List<PaymentEvent>> {
        val since = LocalDate.now().minusMonths(months.toLong() - 1)
            .withDayOfMonth(1)
            .toEpochDay()
        return paymentEventDao.observeSince(since)
    }
}
