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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.home.HomeViewModel


val ExpenseCardContainerColor = Color(0xFFFFFDF8)
val CategoryTagColor = Color(0xFFEAE4D0)
val MutedGold = Color(0xFFDDAA44)

@Composable
fun ExpenseCard(expense: Expense, viewModel: HomeViewModel) {
    var showExpenseModal by remember { mutableStateOf(false)}

    if (showExpenseModal) {
        ExpenseModal(
            onDismiss = { showExpenseModal = false },
            onSave = { expense, name, category, date, amount ->
                Log.d("Update Expense", "Name: $name, Category: $category, Date: $date, Amount: ₹$amount")
                viewModel.updateExpense(expense, name, category, amount, date)
                showExpenseModal = false
            },
            onDelete = { expenseId ->
                Log.d("Delete Expense", "Deleting expense: ${expense.title}")
                viewModel.deleteExpense(expenseId)
                showExpenseModal = false
            },
            title = stringResource(id = R.string.edit_expense_text),
            existingExpense = expense,
            haveDeleteOption = true,
            selectedDate = expense.date
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable { showExpenseModal = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ExpenseCardContainerColor)
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
                    .background(MutedGold, RoundedCornerShape(2.dp))
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

                expense.category.let { category ->
                    Surface(
                        color = CategoryTagColor,
                        shape = RoundedCornerShape(10.dp)
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
                }
            }
        }
    }
}
