package com.hrdevzone.paydue.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hrdevzone.paydue.MainActivity
import com.hrdevzone.paydue.R
import com.hrdevzone.paydue.data.model.DueDirection

object NotificationHelper {

    const val CHANNEL_ID = "paydue_reminders"

    fun ensureChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Due reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for upcoming bills and dues"
            enableLights(true)
            lightColor = Color.rgb(0x1B, 0x8A, 0x5A)
            enableVibration(true)
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showDueNotification(
        context: Context,
        dueId: Long,
        title: String,
        amount: Double,
        currencySymbol: String,
        direction: DueDirection
    ) {
        ensureChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            dueId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val amountText = "$currencySymbol${"%,.2f".format(amount)}"
        val message = if (direction == DueDirection.PAYABLE) {
            "$amountText is due today"
        } else {
            "$amountText is owed to you today"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = NotificationManagerCompat.from(context)
        runCatching { manager.notify(dueId.toInt(), notification) }
    }
}
