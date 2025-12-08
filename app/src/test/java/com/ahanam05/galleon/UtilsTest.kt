package com.ahanam05.galleon

import com.ahanam05.galleon.home.ExpenseItem
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class UtilsTest {
    @Test
    fun isSameDay_sameDaySameTime_returnsTrue() {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()

        val result = isSameDay(calendar1.timeInMillis, calendar2.timeInMillis)

        assertTrue(result)
    }

    @Test
    fun isSameDay_sameDayDifferentTime_returnsTrue() {
        val calendar1 = Calendar.getInstance()
        calendar1.set(2025, Calendar.DECEMBER, 10, 10, 30)
        val calendar2 = Calendar.getInstance()
        calendar2.set(2025, Calendar.DECEMBER, 10, 18, 45)

        val result = isSameDay(calendar1.timeInMillis, calendar2.timeInMillis)

        assertTrue(result)
    }

    @Test
    fun isSameDay_differentDay_returnsFalse() {
        val calendar1 = Calendar.getInstance()
        calendar1.set(2025, Calendar.DECEMBER, 10, 10, 30)
        val calendar2 = Calendar.getInstance()
        calendar2.set(2025, Calendar.DECEMBER, 11, 18, 45)

        val result = isSameDay(calendar1.timeInMillis, calendar2.timeInMillis)

        assertFalse(result)
    }

    @Test
    fun isSameDay_yearBoundary_returnsFalse() {
        val calendar1 = Calendar.getInstance()
        calendar1.set(2025, Calendar.DECEMBER, 31, 23, 59, 59)
        val calendar2 = Calendar.getInstance()
        calendar2.set(2026, Calendar.JANUARY, 1, 0,0,0)

        val result = isSameDay(calendar1.timeInMillis, calendar2.timeInMillis)

        assertFalse(result)
    }

    @Test
    fun formatDate__returnsCorrectlyFormattedString() {
        val calendar = Calendar.getInstance(Locale.US)
        calendar.set(2025, Calendar.DECEMBER, 25)
        val timestamp = calendar.timeInMillis

        val result = formatDate(timestamp)

        Assert.assertEquals("Thursday, December 25", result)
    }

    @Test
    fun getTotalAmount_withMultipleExpenses_returnsCorrectSum() {
        val expenses = listOf(
            ExpenseItem("Coffee", "Food","9.50", 0L),
            ExpenseItem("Movie", "Entertainment","15.00",  0L),
            ExpenseItem("Groceries", "Food","75.50",  0L)
        )

        val result = getTotalAmount(expenses)

        Assert.assertEquals("100.0", result)
    }

    @Test
    fun getTotalAmount_withEmptyList_returnsZero() {
        val expenses = emptyList<ExpenseItem>()

        val result = getTotalAmount(expenses)

        Assert.assertEquals("0.0", result)
    }

    @Test
    fun getTotalAmount_withInvalidAmount_ignoresInvalidValue() {
        val expenses = listOf(
            ExpenseItem("Book", "Entertainment","20.0",  0L),
            ExpenseItem("Corrupted Data", "not_a_number", "Error", 0L),
            ExpenseItem("Snack", "Food","5.0",  0L)
        )

        val result = getTotalAmount(expenses)

        Assert.assertEquals("25.0", result)
    }
}