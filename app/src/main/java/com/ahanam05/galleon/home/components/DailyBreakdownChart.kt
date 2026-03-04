package com.ahanam05.galleon.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.util.*
import com.ahanam05.galleon.R
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.text.SimpleDateFormat

@Composable
fun DailyBreakdownChart(
    dailyBreakdown: Map<String, Double>,
    weekStartDate: Long,
    weeklyTotal: Double,
    topCategory: Pair<String, Double>?,
    dailyAverage: Double
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val calendar = Calendar.getInstance().apply { timeInMillis = weekStartDate }
    val orderedDays = List(7) {
        val dayLabel = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            .format(calendar.time).uppercase()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        dayLabel
    }

    val chartData = orderedDays.map { dayLabel ->
        dailyBreakdown[dayLabel] ?: 0.0
    }

    LaunchedEffect(chartData) {
        modelProducer.runTransaction {
            columnSeries { series(chartData) }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Total Weekly Expense
            Text(
                text = "Total Weekly Expense",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${stringResource(id = R.string.rupee_symbol)}${"%.2f".format(weeklyTotal)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                color = MutedGold,
                                thickness = 14.dp,
                                shape = Shape.rounded(allPercent = 20)
                            )
                        )
                    )
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Day Labels (S M T W T F S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val calendar = Calendar.getInstance().apply { timeInMillis = weekStartDate }
                repeat(7) {
                    val dayLabel = SimpleDateFormat("EEE", Locale.getDefault())
                        .format(calendar.time)
                        .first()
                        .toString()

                    Text(
                        text = dayLabel,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Row: Top Category and Average/Day
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Category
                if (topCategory != null) {
                    Column {
                        Text(
                            text = "Top Category: ${topCategory.first}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedGold
                        )
                        Text(
                            text = "${topCategory.second.toInt()}%",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    Text(
                        text = "No expenses yet",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // Average/Day
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Avg/Day",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${stringResource(id = R.string.rupee_symbol)}${"%.2f".format(dailyAverage)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                }
            }
        }
    }
}
