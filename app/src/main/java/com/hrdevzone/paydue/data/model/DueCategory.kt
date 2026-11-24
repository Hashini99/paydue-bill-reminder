package com.hrdevzone.paydue.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class DueCategory(val label: String) {
    RENT("Rent"),
    SUBSCRIPTION("Subscription"),
    EMI("EMI / Loan"),
    UTILITY("Utility"),
    INSURANCE("Insurance"),
    EDUCATION("Education"),
    SHOPPING("Shopping"),
    CLIENT_WORK("Client work"),
    OTHER("Other");

    val icon: ImageVector
        get() = when (this) {
            RENT -> Icons.Filled.Home
            SUBSCRIPTION -> Icons.Filled.Subscriptions
            EMI -> Icons.Filled.CreditCard
            UTILITY -> Icons.Filled.ElectricBolt
            INSURANCE -> Icons.Filled.HealthAndSafety
            EDUCATION -> Icons.Filled.School
            SHOPPING -> Icons.Filled.ShoppingBag
            CLIENT_WORK -> Icons.Filled.Work
            OTHER -> Icons.Filled.Payments
        }
}
