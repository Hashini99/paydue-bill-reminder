package com.hrdevzone.paydue.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class PayDueStatusColors(
    val paid: Color,
    val paidContainer: Color,
    val overdue: Color,
    val overdueContainer: Color,
    val upcoming: Color,
    val upcomingContainer: Color,
    val receivable: Color,
    val receivableContainer: Color
)

val LocalPayDueStatusColors = staticCompositionLocalOf {
    PayDueStatusColors(
        paid = StatusPaidLight,
        paidContainer = StatusPaidContainerLight,
        overdue = StatusOverdueLight,
        overdueContainer = StatusOverdueContainerLight,
        upcoming = StatusUpcomingLight,
        upcomingContainer = StatusUpcomingContainerLight,
        receivable = StatusReceivableLight,
        receivableContainer = StatusReceivableContainerLight
    )
}

val PayDueLightStatusColors = PayDueStatusColors(
    paid = StatusPaidLight,
    paidContainer = StatusPaidContainerLight,
    overdue = StatusOverdueLight,
    overdueContainer = StatusOverdueContainerLight,
    upcoming = StatusUpcomingLight,
    upcomingContainer = StatusUpcomingContainerLight,
    receivable = StatusReceivableLight,
    receivableContainer = StatusReceivableContainerLight
)

val PayDueDarkStatusColors = PayDueStatusColors(
    paid = StatusPaidDark,
    paidContainer = StatusPaidContainerDark,
    overdue = StatusOverdueDark,
    overdueContainer = StatusOverdueContainerDark,
    upcoming = StatusUpcomingDark,
    upcomingContainer = StatusUpcomingContainerDark,
    receivable = StatusReceivableDark,
    receivableContainer = StatusReceivableContainerDark
)

object PayDueTheme {
    val statusColors: PayDueStatusColors
        @Composable get() = LocalPayDueStatusColors.current
}
