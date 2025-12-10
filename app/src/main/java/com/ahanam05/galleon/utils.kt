package com.ahanam05.galleon

import com.ahanam05.galleon.data.models.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun isSameDay(millis1: Long, millis2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun formatDate(milliseconds: Long): String {
    val formatter = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    return formatter.format(Calendar.getInstance().apply { timeInMillis = milliseconds }.time)
}

fun getTotalAmount(expenses: List<Expense>): String {
    var totalAmount = 0.0
    for (expense in expenses) {
        totalAmount += expense.amount
    }
    return totalAmount.toString()
}