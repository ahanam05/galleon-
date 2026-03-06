package com.ahanam05.galleon.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.data.models.Budget

@Composable
fun BudgetStatusCard(
    monthlyTotal: Double,
    budget: Budget?,
    percentageSpent: Double?,
    remainingAmount: Double,
    formattedMonth: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = "Budget Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2D2D2D)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (budget == null) {
            Text(
                text = "No budget set for $formattedMonth",
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "₹${String.format("%.0f", monthlyTotal)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        text = "of ₹${String.format("%.0f", budget.monthlyBudget)}",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }

                if (percentageSpent != null) {
                    Text(
                        text = "${percentageSpent.toInt()}% spent",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            percentageSpent >= 100 -> Color(0xFFDC3545)
                            percentageSpent >= 75 -> Color(0xFFFFC107)
                            else -> Color(0xFF28A745)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (percentageSpent != null) {
                LinearProgressIndicator(
                    progress = (percentageSpent / 100).toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        percentageSpent >= 100 -> Color(0xFFDC3545)
                        percentageSpent >= 75 -> Color(0xFFFFC107)
                        else -> Color(0xFF28A745)
                    },
                    trackColor = Color(0xFFE0E0E0)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (remainingAmount > 0) {
                    "Remaining: ₹${String.format("%.0f", remainingAmount)}"
                } else {
                    "Budget exhausted for this month"
                },
                fontSize = 12.sp,
                color = if (remainingAmount > 0) Color(0xFF666666) else Color(0xFFDC3545)
            )
        }
    }
}
