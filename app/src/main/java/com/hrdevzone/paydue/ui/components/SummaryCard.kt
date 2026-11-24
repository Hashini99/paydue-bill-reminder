package com.hrdevzone.paydue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hrdevzone.paydue.ui.theme.NavyPrimary
import com.hrdevzone.paydue.ui.theme.NavyPrimaryLight
import com.hrdevzone.paydue.util.formatAmount

@Composable
fun SummaryCard(
    payableTotal: Double,
    receivableTotal: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(listOf(NavyPrimary, NavyPrimaryLight))
            )
            .padding(20.dp)
    ) {
        Text(
            text = "Net upcoming",
            style = MaterialTheme.typography.labelLarge,
            color = androidx.compose.ui.graphics.Color(0xFFB9C6DA)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = formatAmount(receivableTotal - payableTotal, currencySymbol),
            style = MaterialTheme.typography.displaySmall,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryStat(
                label = "You owe",
                amount = formatAmount(payableTotal, currencySymbol),
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                label = "Owed to you",
                amount = formatAmount(receivableTotal, currencySymbol),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryStat(label: String, amount: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = androidx.compose.ui.graphics.Color(0xFFB9C6DA)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = amount,
            style = MaterialTheme.typography.titleMedium,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}
