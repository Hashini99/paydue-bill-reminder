package com.hrdevzone.paydue.data.repository

import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.local.DueDao
import com.hrdevzone.paydue.data.local.PaymentEvent
import com.hrdevzone.paydue.data.model.RepeatType
import com.hrdevzone.paydue.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DueRepository(
    private val dueDao: DueDao,
    private val reminderScheduler: ReminderScheduler,
    private val paymentEventRepository: PaymentEventRepository
) {

    fun observeAll(): Flow<List<Due>> = dueDao.observeAll()

    fun observeById(id: Long): Flow<Due?> = dueDao.observeById(id)

    suspend fun getById(id: Long): Due? = dueDao.getById(id)

    suspend fun save(due: Due): Long {
        val id = dueDao.upsert(due)
        val saved = if (due.id == 0L) due.copy(id = id) else due
        reminderScheduler.scheduleReminder(saved)
        return id
    }

    suspend fun delete(due: Due) {
        reminderScheduler.cancelReminder(due.id)
        dueDao.delete(due)
    }

    suspend fun markPaid(due: Due) {
        val today = LocalDate.now().toEpochDay()
        val updated = if (due.repeatType == RepeatType.ONCE) {
            due.copy(isPaid = true, lastPaidEpochDay = today)
        } else {
            val nextDate = advance(LocalDate.ofEpochDay(due.dueDateEpochDay), due.repeatType)
            due.copy(
                isPaid = false,
                dueDateEpochDay = nextDate.toEpochDay(),
                lastPaidEpochDay = today
            )
        }
        dueDao.update(updated)
        reminderScheduler.cancelReminder(due.id)
        reminderScheduler.scheduleReminder(updated)
        paymentEventRepository.record(
            PaymentEvent(
                dueId = due.id,
                title = due.title,
                category = due.category,
                direction = due.direction,
                amount = due.amount,
                paidEpochDay = today
            )
        )
    }

    suspend fun markUnpaid(due: Due) {
        val updated = due.copy(isPaid = false, lastPaidEpochDay = null)
        dueDao.update(updated)
        reminderScheduler.scheduleReminder(updated)
    }

    suspend fun getAllOnce(): List<Due> = dueDao.getAllOnce()

    suspend fun replaceAll(dues: List<Due>) {
        dueDao.clearAll()
        dueDao.insertAll(dues)
        dues.forEach { reminderScheduler.scheduleReminder(it) }
    }

    suspend fun count(): Int = dueDao.count()

    private fun advance(date: LocalDate, repeatType: RepeatType): LocalDate {
        var next = when (repeatType) {
            RepeatType.MONTHLY -> date.plusMonths(1)
            RepeatType.YEARLY -> date.plusYears(1)
            RepeatType.ONCE -> date
        }
        val today = LocalDate.now()
        while (next.isBefore(today)) {
            next = when (repeatType) {
                RepeatType.MONTHLY -> next.plusMonths(1)
                RepeatType.YEARLY -> next.plusYears(1)
                RepeatType.ONCE -> return next
            }
        }
        return next
    }
}
