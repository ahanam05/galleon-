package com.ahanam05.galleon

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
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
