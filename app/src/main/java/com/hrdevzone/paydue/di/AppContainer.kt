package com.hrdevzone.paydue.di

import android.content.Context
import com.hrdevzone.paydue.data.backup.BackupManager
import com.hrdevzone.paydue.data.local.PayDueDatabase
import com.hrdevzone.paydue.data.prefs.UserPreferences
import com.hrdevzone.paydue.data.repository.DueRepository
import com.hrdevzone.paydue.data.repository.InvoiceRepository
import com.hrdevzone.paydue.data.repository.PaymentEventRepository
import com.hrdevzone.paydue.notification.ReminderScheduler
import com.hrdevzone.paydue.util.AppLockState

class AppContainer(context: Context) {

    private val database by lazy { PayDueDatabase.getInstance(context) }

    val userPreferences by lazy { UserPreferences(context) }

    val reminderScheduler by lazy { ReminderScheduler(context, userPreferences) }

    val paymentEventRepository by lazy { PaymentEventRepository(database.paymentEventDao()) }

    val dueRepository by lazy {
        DueRepository(database.dueDao(), reminderScheduler, paymentEventRepository)
    }

    val invoiceRepository by lazy { InvoiceRepository(database.invoiceDao()) }

    val backupManager by lazy { BackupManager(context) }

    val appLockState by lazy { AppLockState() }
}
