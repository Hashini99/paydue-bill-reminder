package com.hrdevzone.paydue.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.prefs.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context,
    private val userPreferences: UserPreferences
) {

    companion object {
        const val EXTRA_DUE_ID = "extra_due_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_CURRENCY = "extra_currency"
        const val EXTRA_DIRECTION = "extra_direction"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleReminder(due: Due) {
        if (!due.reminderEnabled || due.isPaid) {
            cancelReminder(due.id)
            return
        }
        val settings = userPreferences.settings.first()
        val date = LocalDate.ofEpochDay(due.dueDateEpochDay)
        val dateTime = date.atTime(settings.reminderHour, settings.reminderMinute)
        val triggerMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerMillis <= System.currentTimeMillis()) {
            cancelReminder(due.id)
            return
        }

        val pendingIntent = buildPendingIntent(due, settings.currencySymbol)

        try {
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    fun cancelReminder(dueId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            dueId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun buildPendingIntent(due: Due, currencySymbol: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_DUE_ID, due.id)
            putExtra(EXTRA_TITLE, due.title)
            putExtra(EXTRA_AMOUNT, due.amount)
            putExtra(EXTRA_CURRENCY, currencySymbol)
            putExtra(EXTRA_DIRECTION, due.direction.name)
        }
        return PendingIntent.getBroadcast(
            context,
            due.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
