package com.hrdevzone.paydue.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object Invoices : Destination("invoices")
    data object Insights : Destination("insights")
    data object Settings : Destination("settings")
    data object Backup : Destination("backup")
    data object PinSetup : Destination("pin_setup")

    data object AddEditDue : Destination("add_edit_due?dueId={dueId}") {
        const val ARG_DUE_ID = "dueId"
        fun route(dueId: Long? = null) = "add_edit_due?dueId=${dueId ?: -1L}"
    }

    data object InvoiceForm : Destination("invoice_form?dueId={dueId}") {
        const val ARG_DUE_ID = "dueId"
        fun route(dueId: Long? = null) = "invoice_form?dueId=${dueId ?: -1L}"
    }

    data object InvoiceView : Destination("invoice_view/{invoiceId}") {
        const val ARG_INVOICE_ID = "invoiceId"
        fun route(invoiceId: Long) = "invoice_view/$invoiceId"
    }
}

data class BottomTab(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomTabs = listOf(
    BottomTab(Destination.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(Destination.Invoices, "Invoices", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomTab(Destination.Insights, "Insights", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    BottomTab(Destination.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)
