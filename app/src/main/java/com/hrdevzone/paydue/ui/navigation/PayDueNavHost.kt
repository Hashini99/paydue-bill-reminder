package com.hrdevzone.paydue.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hrdevzone.paydue.ui.addedit.AddEditDueScreen
import com.hrdevzone.paydue.ui.backup.BackupScreen
import com.hrdevzone.paydue.ui.home.HomeScreen
import com.hrdevzone.paydue.ui.insights.InsightsScreen
import com.hrdevzone.paydue.ui.invoice.InvoiceFormScreen
import com.hrdevzone.paydue.ui.invoice.InvoiceListScreen
import com.hrdevzone.paydue.ui.invoice.InvoiceViewScreen
import com.hrdevzone.paydue.ui.lock.PinSetupScreen
import com.hrdevzone.paydue.ui.settings.SettingsScreen

private val topLevelRoutes = setOf(
    Destination.Home.route,
    Destination.Invoices.route,
    Destination.Insights.route,
    Destination.Settings.route
)

@Composable
fun PayDueNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any {
                            it.route == tab.destination.route
                        } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        ) {
            composableRoute(Destination.Home.route) {
                HomeScreen(
                    onAddDue = { navController.navigate(Destination.AddEditDue.route(null)) },
                    onEditDue = { id -> navController.navigate(Destination.AddEditDue.route(id)) }
                )
            }

            composableRoute(Destination.Invoices.route) {
                InvoiceListScreen(
                    onCreateInvoice = { navController.navigate(Destination.InvoiceForm.route(null)) },
                    onOpenInvoice = { id -> navController.navigate(Destination.InvoiceView.route(id)) }
                )
            }

            composableRoute(Destination.Insights.route) {
                InsightsScreen()
            }

            composableRoute(Destination.Settings.route) {
                SettingsScreen(
                    onOpenBackup = { navController.navigate(Destination.Backup.route) },
                    onOpenPinSetup = { navController.navigate(Destination.PinSetup.route) }
                )
            }

            composableRoute(Destination.Backup.route) {
                BackupScreen(onBack = { navController.popBackStack() })
            }

            composableRoute(Destination.PinSetup.route) {
                PinSetupScreen(
                    onBack = { navController.popBackStack() },
                    onCompleted = { navController.popBackStack() }
                )
            }

            composable(
                route = Destination.AddEditDue.route,
                arguments = listOf(navArgument(Destination.AddEditDue.ARG_DUE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                val id = entry.arguments?.getLong(Destination.AddEditDue.ARG_DUE_ID) ?: -1L
                AddEditDueScreen(
                    dueId = if (id < 0) null else id,
                    onDone = { navController.popBackStack() }
                )
            }

            composable(
                route = Destination.InvoiceForm.route,
                arguments = listOf(navArgument(Destination.InvoiceForm.ARG_DUE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { entry ->
                val id = entry.arguments?.getLong(Destination.InvoiceForm.ARG_DUE_ID) ?: -1L
                InvoiceFormScreen(
                    prefillDueId = if (id < 0) null else id,
                    onBack = { navController.popBackStack() },
                    onGenerated = { invoiceId ->
                        navController.navigate(Destination.InvoiceView.route(invoiceId)) {
                            popUpTo(Destination.Invoices.route)
                        }
                    }
                )
            }

            composable(
                route = Destination.InvoiceView.route,
                arguments = listOf(navArgument(Destination.InvoiceView.ARG_INVOICE_ID) {
                    type = NavType.LongType
                })
            ) { entry ->
                val id = entry.arguments?.getLong(Destination.InvoiceView.ARG_INVOICE_ID) ?: 0L
                InvoiceViewScreen(
                    invoiceId = id,
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavGraphBuilder.composableRoute(
    route: String,
    content: @Composable () -> Unit
) {
    composable(route) { content() }
}
