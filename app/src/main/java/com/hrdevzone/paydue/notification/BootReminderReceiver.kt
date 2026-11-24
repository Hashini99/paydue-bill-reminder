package com.hrdevzone.paydue.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hrdevzone.paydue.PayDueApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val app = context.applicationContext as PayDueApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dues = app.container.dueRepository.getAllOnce()
                dues.forEach { app.container.reminderScheduler.scheduleReminder(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
