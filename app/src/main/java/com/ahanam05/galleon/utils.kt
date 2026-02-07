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

fun getWeekStartDate(dateMillis: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getWeekEndDate(dateMillis: Long): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

    while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

fun getWeekRange(dateMillis: Long): Pair<Long, Long> {
    return Pair(getWeekStartDate(dateMillis), getWeekEndDate(dateMillis))
}

fun isInWeek(dateToCheck: Long, weekStartDateMillis: Long, weekEndDateMillis: Long): Boolean {
    return dateToCheck >= weekStartDateMillis && dateToCheck <= weekEndDateMillis
}

fun formatWeekRange(startDateMillis: Long, endDateMillis: Long): String {
    val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
    val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }

    val startMonth = startCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
    val endMonth = endCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
    val startDay = startCal.get(Calendar.DAY_OF_MONTH)
    val endDay = endCal.get(Calendar.DAY_OF_MONTH)

    return if (startMonth == endMonth) {
        "$startMonth $startDay - $endDay"
    } else {
        "$startMonth $startDay - $endMonth $endDay"
    }
}
