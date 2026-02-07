package com.ahanam05.galleon.data.aggregator

import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.getWeekStartDate
import com.ahanam05.galleon.getWeekEndDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ExpenseAggregatorTest {

    private fun createExpense(title: String, amount: Double, dateMillis: Long): Expense {
        return Expense(
            id = "",
            title = title,
            amount = amount,
            category = "Test",
            date = dateMillis
        )
    }

    @Test
    fun getWeeklyTotal_withExpensesInWeek_returnsSum() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5) // Wednesday
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        val expenses = listOf(
            createExpense("Coffee", 5.0, weekStart + 86400000), // Monday
            createExpense("Lunch", 10.0, weekStart + 172800000), // Tuesday
            createExpense("Dinner", 15.0, weekStart + 259200000) // Wednesday
        )

        val result = ExpenseAggregator.getWeeklyTotal(expenses, weekStart, weekEnd)

        assertEquals(30.0, result, 0.01)
    }

    @Test
    fun getWeeklyTotal_withNoExpenses_returnsZero() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        val result = ExpenseAggregator.getWeeklyTotal(emptyList(), weekStart, weekEnd)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun getDailyTotalForDate_withMultipleExpenses_returnsSum() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)
        val dateMillis = calendar.timeInMillis

        val expenses = listOf(
            createExpense("Coffee", 5.0, dateMillis),
            createExpense("Lunch", 10.0, dateMillis),
            createExpense("Dinner", 15.0, dateMillis)
        )

        val result = ExpenseAggregator.getDailyTotalForDate(expenses, dateMillis)

        assertEquals(30.0, result, 0.01)
    }

    @Test
    fun getDailyTotalForDate_withDifferentDay_returnsZero() {
        val calendar1 = Calendar.getInstance()
        calendar1.set(2025, Calendar.FEBRUARY, 5)
        val calendar2 = Calendar.getInstance()
        calendar2.set(2025, Calendar.FEBRUARY, 6)

        val expenses = listOf(
            createExpense("Coffee", 5.0, calendar1.timeInMillis)
        )

        val result = ExpenseAggregator.getDailyTotalForDate(expenses, calendar2.timeInMillis)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun getWeeklyAveragePerDay_dividesWeeklyTotalBy7() {
        val weeklyTotal = 700.0

        val result = ExpenseAggregator.getWeeklyAveragePerDay(weeklyTotal)

        assertEquals(100.0, result, 0.01)
    }

    @Test
    fun getWeeklyAveragePerDay_roundsTo2Decimals() {
        val weeklyTotal = 100.0

        val result = ExpenseAggregator.getWeeklyAveragePerDay(weeklyTotal)

        assertEquals(14.29, result, 0.01)
    }

    @Test
    fun getTopCategoryByExpense_returnsHighestSpendingCategory() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        val expenses = listOf(
            Expense("1", "Coffee", 10.0, "Food", weekStart + 86400000),
            Expense("2", "Lunch", 15.0, "Food", weekStart + 172800000),
            Expense("3", "Metro", 30.0, "Transport", weekStart + 259200000)
        )

        val result = ExpenseAggregator.getTopCategoryByExpense(expenses, weekStart, weekEnd)

        assertEquals("Transport", result?.first)
        assertEquals(55.0, result?.second)
    }

    @Test
    fun getTopCategoryByExpense_withEmptyExpenses_returnsNull() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        val result = ExpenseAggregator.getTopCategoryByExpense(emptyList(), weekStart, weekEnd)

        assertNull(result)
    }

    @Test
    fun getWeeklyDailyBreakdown_returnsMapWith7Entries() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 2) // Sunday
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        val expenses = listOf(
            createExpense("Sun Expense", 10.0, weekStart),
            createExpense("Mon Expense", 20.0, weekStart + 86400000),
            createExpense("Tue Expense", 30.0, weekStart + 172800000)
        )

        val result = ExpenseAggregator.getWeeklyDailyBreakdown(expenses, weekStart, weekEnd)

        assertEquals(7, result.size)
        assertEquals(10.0, result.values.first(), 0.01)
    }

    @Test
    fun getDayLabel_formatsCorrectly() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.DECEMBER, 9) // Tuesday

        val result = ExpenseAggregator.getDayLabel(calendar.timeInMillis)

        assertEquals(true, result.contains("DEC"))
        assertEquals(true, result.contains("9"))
    }

    @Test
    fun getExpensesForDay_filtersCorrectly() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)
        val dateMillis = calendar.timeInMillis

        val expenses = listOf(
            createExpense("Day 1 Expense 1", 10.0, dateMillis),
            createExpense("Day 1 Expense 2", 20.0, dateMillis + 3600000),
            createExpense("Day 1", 30.0, dateMillis + 86400000)
        )

        val result = ExpenseAggregator.getExpensesForDay(expenses, dateMillis)

        assertEquals(2, result.size)
        assertEquals(30.0, result.sumOf { it.amount }, 0.01)
    }
}
