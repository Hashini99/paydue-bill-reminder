package com.hrdevzone.paydue.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hrdevzone.paydue.data.local.Due
import com.hrdevzone.paydue.data.model.DueDirection
import com.hrdevzone.paydue.ui.theme.PayDueTheme
import com.hrdevzone.paydue.util.formatAmount
import com.hrdevzone.paydue.util.relativeDueLabel
import java.time.LocalDate

@Composable
fun DueCard(
    due: Due,
    currencySymbol: String,
    onClick: () -> Unit,
    onTogglePaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = !due.isPaid && LocalDate.ofEpochDay(due.dueDateEpochDay).isBefore(LocalDate.now())
    val status = PayDueTheme.statusColors

    val (accentColor, accentContainer) = when {
        due.isPaid -> status.paid to status.paidContainer
        isOverdue -> status.overdue to status.overdueContainer
        due.direction == DueDirection.RECEIVABLE -> status.receivable to status.receivableContainer
        else -> status.upcoming to status.upcomingContainer
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(accentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = due.category.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = due.title,
                        style = MaterialTheme.typography.titleMedium,
                        textDecoration = if (due.isPaid) TextDecoration.LineThrough else null,
                        color = if (due.isPaid) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    DirectionGlyph(direction = due.direction)
                }
                Text(
                    text = if (due.isPaid) "Paid" else relativeDueLabel(due.dueDateEpochDay),
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatAmount(due.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onTogglePaid) {
                    Icon(
                        imageVector = if (due.isPaid) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                        contentDescription = if (due.isPaid) "Mark unpaid" else "Mark paid",
                        tint = if (due.isPaid) status.paid else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DirectionGlyph(direction: DueDirection) {
    val status = PayDueTheme.statusColors
    val (icon, tint) = if (direction == DueDirection.RECEIVABLE) {
        Icons.Filled.SouthWest to status.receivable
    } else {
        Icons.Filled.NorthEast to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .size(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (direction == DueDirection.RECEIVABLE) "Owed to you" else "You owe",
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
    }
}
