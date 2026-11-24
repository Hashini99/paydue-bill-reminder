package com.hrdevzone.paydue.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MonthBar(
    val label: String,
    val payable: Double,
    val receivable: Double
)

@Composable
fun MonthlyBarChart(
    bars: List<MonthBar>,
    payableColor: Color,
    receivableColor: Color,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "barProgress"
    )
    LaunchedEffect(bars) { animationPlayed = true }

    val maxValue = (bars.maxOfOrNull { maxOf(it.payable, it.receivable) } ?: 0.0).coerceAtLeast(1.0)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val axisLabelHeight = 36f
            val chartHeight = size.height - axisLabelHeight
            val groupWidth = size.width / bars.size
            val barWidth = (groupWidth * 0.28f).coerceAtMost(28.dp.toPx())
            val gap = barWidth * 0.25f

            drawLine(
                color = gridColor,
                start = Offset(0f, chartHeight),
                end = Offset(size.width, chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            bars.forEachIndexed { index, bar ->
                val centerX = groupWidth * index + groupWidth / 2f
                val payableHeight = (bar.payable / maxValue).toFloat() * chartHeight * progress
                val receivableHeight = (bar.receivable / maxValue).toFloat() * chartHeight * progress

                drawRoundRect(
                    color = payableColor,
                    topLeft = Offset(centerX - barWidth - gap / 2f, chartHeight - payableHeight),
                    size = Size(barWidth, payableHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = receivableColor,
                    topLeft = Offset(centerX + gap / 2f, chartHeight - receivableHeight),
                    size = Size(barWidth, receivableHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(bar.label, centerX, size.height - 8f, paint)
                }
            }
        }
    }
}
