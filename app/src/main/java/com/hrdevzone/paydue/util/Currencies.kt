package com.hrdevzone.paydue.util

data class CurrencyOption(
    val code: String,
    val symbol: String,
    val displayName: String
)

val currencyOptions = listOf(
    CurrencyOption("INR", "₹", "Indian Rupee"),
    CurrencyOption("USD", "$", "US Dollar"),
    CurrencyOption("EUR", "€", "Euro"),
    CurrencyOption("GBP", "£", "British Pound"),
    CurrencyOption("JPY", "¥", "Japanese Yen"),
    CurrencyOption("AUD", "A$", "Australian Dollar"),
    CurrencyOption("CAD", "C$", "Canadian Dollar"),
    CurrencyOption("SGD", "S$", "Singapore Dollar"),
    CurrencyOption("AED", "د.إ", "UAE Dirham"),
    CurrencyOption("LKR", "Rs", "Sri Lankan Rupee"),
    CurrencyOption("NPR", "Rs", "Nepalese Rupee"),
    CurrencyOption("PKR", "₨", "Pakistani Rupee"),
    CurrencyOption("ZAR", "R", "South African Rand"),
    CurrencyOption("NGN", "₦", "Nigerian Naira"),
    CurrencyOption("BRL", "R$", "Brazilian Real")
)

fun currencyOptionForCode(code: String): CurrencyOption =
    currencyOptions.find { it.code == code } ?: currencyOptions.first()
