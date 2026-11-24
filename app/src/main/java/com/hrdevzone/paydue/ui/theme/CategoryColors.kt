package com.hrdevzone.paydue.ui.theme

import androidx.compose.ui.graphics.Color
import com.hrdevzone.paydue.data.model.DueCategory

private val CategoryPaletteLight = mapOf(
    DueCategory.RENT to Color(0xFF1B365D),
    DueCategory.SUBSCRIPTION to Color(0xFF1B9C85),
    DueCategory.EMI to Color(0xFFD64545),
    DueCategory.UTILITY to Color(0xFFB8860B),
    DueCategory.INSURANCE to Color(0xFF6A4FB6),
    DueCategory.EDUCATION to Color(0xFF1B7FA8),
    DueCategory.SHOPPING to Color(0xFFC2578B),
    DueCategory.CLIENT_WORK to Color(0xFF3E8E4F),
    DueCategory.OTHER to Color(0xFF8A8F98)
)

private val CategoryPaletteDark = mapOf(
    DueCategory.RENT to Color(0xFF6E93C4),
    DueCategory.SUBSCRIPTION to Color(0xFF4CD6AE),
    DueCategory.EMI to Color(0xFFFF7A7A),
    DueCategory.UTILITY to Color(0xFFF0C05A),
    DueCategory.INSURANCE to Color(0xFFAE97E8),
    DueCategory.EDUCATION to Color(0xFF6FC7EC),
    DueCategory.SHOPPING to Color(0xFFEC9AC0),
    DueCategory.CLIENT_WORK to Color(0xFF8FDB9E),
    DueCategory.OTHER to Color(0xFFAAB0BB)
)

fun categoryColor(category: DueCategory, isDark: Boolean): Color =
    (if (isDark) CategoryPaletteDark else CategoryPaletteLight).getValue(category)
