package com.hrdevzone.paydue.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hrdevzone.paydue.data.model.DueDirection

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dueId = intent.getLongExtra(ReminderScheduler.EXTRA_DUE_ID, -1L)
        if (dueId < 0) return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Due reminder"
        val amount = intent.getDoubleExtra(ReminderScheduler.EXTRA_AMOUNT, 0.0)
        val currency = intent.getStringExtra(ReminderScheduler.EXTRA_CURRENCY) ?: "₹"
        val direction = intent.getStringExtra(ReminderScheduler.EXTRA_DIRECTION)
            ?.let { runCatching { DueDirection.valueOf(it) }.getOrNull() }
            ?: DueDirection.PAYABLE

        NotificationHelper.showDueNotification(
            context = context,
            dueId = dueId,
            title = title,
            amount = amount,
            currencySymbol = currency,
            direction = direction
        )
    }
}
