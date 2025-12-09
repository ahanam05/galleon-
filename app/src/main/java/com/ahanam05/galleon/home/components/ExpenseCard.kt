package com.ahanam05.galleon.home.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.R
import com.ahanam05.galleon.home.ExpenseItem

@Composable
fun ExpenseCard(expense: ExpenseItem) {
    var showExpenseModal by remember { mutableStateOf(false)}

    if (showExpenseModal) {
        ExpenseModal(
            onDismiss = { showExpenseModal = false },
            onSave = { name, category, date, amount ->
                // TODO: Handle updating the expense in the database
                Log.d("Edit Expense", "Name: $name, Category: $category, Date: $date, Amount: ₹$amount")
                showExpenseModal = false
            },
            onDelete = {
                // TODO: Handle deleting the expense from the database
                Log.d("Delete Expense", "Deleting expense: ${expense.title}")
                showExpenseModal = false
            },
            title = stringResource(id = R.string.edit_expense_text),
            existingExpense = expense,
            haveDeleteOption = true
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7EA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(48.dp)
                    .background(Color(0xFFE0B663), RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )

                Spacer(modifier = Modifier.weight(1f))

                expense.category.let { category ->
                    Surface(
                        color = expense.categoryColor,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 9.sp,
                            color = Color(0xFF6B6B6B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(){
                    Text(
                        text = "${stringResource(id = R.string.rupee_symbol)}${expense.amount}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.edit_desc),
                        modifier = Modifier.size(20.dp)
                            .clickable(
                                onClick = {
                                    showExpenseModal = true
                                }
                            ),
                        tint = Color(0xFF2D2D2D),
                    )
                }
            }
        }
    }
}
