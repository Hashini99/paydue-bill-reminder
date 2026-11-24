package com.hrdevzone.paydue.data.model

enum class DueDirection(val label: String, val shortLabel: String) {
    PAYABLE("I need to pay", "You owe"),
    RECEIVABLE("Owed to me", "Owed to you")
}
