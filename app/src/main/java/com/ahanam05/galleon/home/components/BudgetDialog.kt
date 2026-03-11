package com.ahanam05.galleon.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ahanam05.galleon.data.models.Budget
import com.ahanam05.galleon.getDaysInMonth
import com.ahanam05.galleon.R

@Composable
fun BudgetDialog(
    formattedMonth: String,
    monthYear: String,
    existingBudget: Budget?,
    onSave: (Budget) -> Unit,
    onDismiss: () -> Unit
) {
    var budgetAmount by remember { mutableStateOf(existingBudget?.monthlyBudget?.toString() ?: "") }
    var showError by remember { mutableStateOf(false) }

    val MutedGold = Color(0xFFDDAA44)
    val BackgroundBeige = Color(0xFFF9F7F0)
    val DarkGray = Color(0xFF2D2D2D)

    val daysInMonth = getDaysInMonth(monthYear)
    val dailyLimit = budgetAmount.toDoubleOrNull()?.let { it / daysInMonth } ?: 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundBeige,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$formattedMonth Budget",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.close_desc),
                            tint = DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MutedGold.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.rupee_symbol),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (existingBudget == null) stringResource(id = R.string.set_monthly_budget) else stringResource(id = R.string.edit_monthly_budget),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.plan_monthly_spending),
                    fontSize = 14.sp,
                    color = DarkGray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column {
                    Text(
                        text = stringResource(id = R.string.total_monthly_budget),
                        fontSize = 14.sp,
                        color = DarkGray.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = budgetAmount,
                        onValueChange = {
                            budgetAmount = it
                            showError = false
                        },
                        placeholder = { Text("0.00", color = DarkGray.copy(alpha = 0.3f)) },
                        leadingIcon = {
                            Text(
                                text = stringResource(id = R.string.rupee_symbol),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedGold
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MutedGold,
                            unfocusedBorderColor = DarkGray.copy(alpha = 0.3f),
                            focusedTextColor = DarkGray,
                            unfocusedTextColor = DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showError
                    )

                    if (showError) {
                        Text(
                            text = stringResource(id = R.string.enter_valid_budget),
                            fontSize = 12.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MutedGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.daily_spending_limit),
                        fontSize = 14.sp,
                        color = DarkGray.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "₹%.2f".format(dailyLimit),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedGold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val amount = budgetAmount.toDoubleOrNull()
                        if (amount == null || amount <= 0) {
                            showError = true} else {
                            val budget = Budget(
                                monthYear = monthYear,
                                monthlyBudget = amount
                            )
                            onSave(budget)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MutedGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.save_budget),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel_text),
                        fontSize = 14.sp,
                        color = DarkGray.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
