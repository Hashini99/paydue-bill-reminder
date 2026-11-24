package com.hrdevzone.paydue.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun formatAmount(amount: Double, currencySymbol: String): String {
    return "$currencySymbol${"%,.2f".format(amount)}"
}

private val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

fun formatDate(epochDay: Long): String {
    return LocalDate.ofEpochDay(epochDay).format(displayDateFormatter)
}

fun relativeDueLabel(epochDay: Long): String {
    val today = LocalDate.now()
    val date = LocalDate.ofEpochDay(epochDay)
    val days = ChronoUnit.DAYS.between(today, date)
    return when {
        days == 0L -> "Due today"
        days == 1L -> "Due tomorrow"
        days == -1L -> "1 day overdue"
        days < -1L -> "${-days} days overdue"
        days in 2..6 -> "Due in $days days"
        else -> "Due ${formatDate(epochDay)}"
    }
}
