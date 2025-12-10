package com.ahanam05.galleon.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.ahanam05.galleon.R
import com.ahanam05.galleon.data.models.Expense

object Categories {
    const val FOOD = "Food"
    const val TRANSPORTATION = "Transportation"
    const val HOUSING = "Housing"
    const val UTILITIES = "Utilities"
    const val INVESTMENTS = "Investments"
    const val ENTERTAINMENT = "Entertainment"
    const val HEALTHCARE = "Healthcare"
    const val MISCELLANEOUS = "Miscellaneous"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseModal(
    onDismiss: () -> Unit,
    onSave: (String, String, Long, String) -> Unit,
    onDelete: () -> Unit,
    existingExpense: Expense?,
    title: String,
    haveDeleteOption: Boolean = false
) {
    val defaultExpenseName = existingExpense?.title ?: ""
    val defaultCategory = existingExpense?.category ?: Categories.FOOD
    val defaultDate = existingExpense?.date ?: System.currentTimeMillis()
    val defaultAmount = existingExpense?.amount ?: "0.00"

    var expenseName by remember { mutableStateOf(defaultExpenseName) }
    var selectedCategory by remember { mutableStateOf(defaultCategory) }
    var selectedDate by remember { mutableLongStateOf(defaultDate)}
    var amount by remember { mutableStateOf(defaultAmount) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var expenseNameError by remember { mutableStateOf(false)}
    var amountError by remember { mutableStateOf(false)}

    val categories = listOf(Categories.FOOD,
        Categories.TRANSPORTATION,
        Categories.HOUSING,
        Categories.UTILITIES,
        Categories.INVESTMENTS,
        Categories.ENTERTAINMENT,
        Categories.HEALTHCARE,
        Categories.MISCELLANEOUS)

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF3A3A3A),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(){
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if(haveDeleteOption){
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.delete_desc),
                                tint = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(id = R.string.category_text),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showCategoryDropdown) Color.White else Color(0xFF2D2D2D),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCategory,
                                color = if (showCategoryDropdown) Color.Black else Color.White,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = stringResource(id = R.string.dropdown_desc),
                                tint = Color.Gray
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier
                            .width(250.dp)
                            .background(Color(0xFF2D2D2D))
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category,
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.expense_name_text),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = expenseName,
                    onValueChange = {
                        expenseName = it
                        expenseNameError = it.isEmpty()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFF2D2D2D),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
                    singleLine = true,
                    isError = expenseNameError
                )

                if (expenseNameError) {
                    Text(
                        text = stringResource(id = R.string.expense_name_error),
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.date_text),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2D2D2D),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateFormatter.format(Date(selectedDate)),
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(id = R.string.calendar_icon_desc),
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.amount_text),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = amount.toString(),
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amount = it
                            amountError = amount.toString().isEmpty()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text(
                            text = stringResource(id = R.string.rupee_symbol),
                            color = Color.Gray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFF2D2D2D),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = amountError
                )

                if (amountError) {
                    Text(
                        text = stringResource(id = R.string.amount_error_message),
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (expenseName.isNotEmpty() && amount.toString().isNotEmpty()) {
                                onSave(expenseName, selectedCategory, selectedDate, amount.toString())
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0B663),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.save_text),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(Color.Gray)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.cancel_text),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { date ->
                if (date != null) {
                    selectedDate = date
                }
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }
}
