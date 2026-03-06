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

    @Test
    fun getMonthlyTotal_withExpensesInMonth_returnsSum() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 1)
        val monthStart = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val monthEnd = calendar.timeInMillis
        val expenses = listOf(
            createExpense("Expense 1", 100.0, monthStart + 86400000),
            createExpense("Expense 2", 200.0, monthStart + 172800000),
            createExpense("Expense 3", 300.0, monthStart + 259200000)
        )

        val result = ExpenseAggregator.getMonthlyTotal(expenses, monthStart, monthEnd)

        assertEquals(600.0, result, 0.01)
    }

    @Test
    fun getMonthlyTotal_withNoExpenses_returnsZero() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 1, 0, 0, 0)
        val monthStart = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = calendar.timeInMillis

        val result = ExpenseAggregator.getMonthlyTotal(emptyList(), monthStart, monthEnd)

        assertEquals(0.0, result, 0.01)
    }

    @Test
    fun getMonthlyComparison_withIncrease_returnsCorrectText() {
        val result = ExpenseAggregator.getMonthlyComparison(1200.0, 1000.0)

        assertEquals("20% more than last month", result.first)
        assertEquals(true, result.second)
        assertEquals(true, result.third)
    }

    @Test
    fun getMonthlyComparison_withDecrease_returnsCorrectText() {
        val result = ExpenseAggregator.getMonthlyComparison(800.0, 1000.0)

        assertEquals("20% less than last month", result.first)
        assertEquals(false, result.second)
        assertEquals(true, result.third)
    }

    @Test
    fun getMonthlyComparison_withZeroCurrentMonth_shouldNotShow() {
        val result = ExpenseAggregator.getMonthlyComparison(0.0, 1000.0)

        assertEquals(false, result.third)
    }

    @Test
    fun getMonthlyComparison_withZeroPreviousMonth_shouldNotShow() {
        val result = ExpenseAggregator.getMonthlyComparison(1000.0, 0.0)

        assertEquals(false, result.third)
    }

    @Test
    fun getMonthlyComparison_roundsPercentageCorrectly() {
        val result = ExpenseAggregator.getMonthlyComparison(1125.0, 1000.0)

        assertEquals("12% more than last month", result.first)
    }

    @Test
    fun getMonthlyComparison_withChangeLessThanOnePercent_shouldNotShow() {
        val result = ExpenseAggregator.getMonthlyComparison(1004.0, 1000.0)

        assertEquals(false, result.third)
    }

    @Test
    fun getMonthlyComparison_withChangeExactlyOnePercent_shouldShow() {
        val result = ExpenseAggregator.getMonthlyComparison(1010.0, 1000.0)

        assertEquals("1% more than last month", result.first)
        assertEquals(true, result.second)
        assertEquals(true, result.third)
    }

    @Test
    fun getMonthlyComparison_withNegativeChangeLessThanOnePercent_shouldNotShow() {
        val result = ExpenseAggregator.getMonthlyComparison(996.0, 1000.0)

        assertEquals(false, result.third)
    }

    @Test
    fun getMonthlyComparison_withZeroPointFivePercentChange_shouldNotShow() {
        val result = ExpenseAggregator.getMonthlyComparison(1005.0, 1000.0)

        assertEquals(false, result.third)
    }

    @Test
    fun getBudgetPercentageSpent_returnsCorrectPercentage() {
        val spent = 500.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(50.0, result)
    }

    @Test
    fun getBudgetPercentageSpent_roundsToNearestInteger() {
        val spent = 337.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(34.0, result)
    }

    @Test
    fun getBudgetPercentageSpent_returnsNullWhenLessThanOnePercent() {
        val spent = 5.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(null, result)
    }

    @Test
    fun getBudgetPercentageSpent_returnsNullWhenZeroBudget() {
        val spent = 500.0
        val budget = 0.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(null, result)
    }

    @Test
    fun getBudgetPercentageSpent_handlesOverBudget() {
        val spent = 1500.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(150.0, result)
    }

    @Test
    fun getBudgetPercentageSpent_handlesZeroSpent() {
        val spent = 0.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(null, result)
    }

    @Test
    fun getBudgetPercentageSpent_handlesExactlyOnePercent() {
        val spent = 10.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(1.0, result)
    }

    @Test
    fun getBudgetPercentageSpent_handles99Point9Percent() {
        val spent = 999.0
        val budget = 1000.0

        val result = ExpenseAggregator.getBudgetPercentageSpent(spent, budget)

        assertEquals(100.0, result)
    }

    @Test
    fun getRemainingBudget_returnsCorrectAmount() {
        val spent = 300.0
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(700.0, result, 0.001)
    }

    @Test
    fun getRemainingBudget_returnsZeroWhenOverBudget() {
        val spent = 1500.0
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun getRemainingBudget_returnsZeroWhenExactlyAtBudget() {
        val spent = 1000.0
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun getRemainingBudget_handlesFractionalAmounts() {
        val spent = 537.25
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(462.75, result, 0.001)
    }

    @Test
    fun getRemainingBudget_handlesZeroSpent() {
        val spent = 0.0
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(1000.0, result, 0.001)
    }

    @Test
    fun getRemainingBudget_handlesZeroBudget() {
        val spent = 500.0
        val budget = 0.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(0.0, result, 0.001)
    }

    @Test
    fun getRemainingBudget_handlesSlightlyOverBudget() {
        val spent = 1000.01
        val budget = 1000.0

        val result = ExpenseAggregator.getRemainingBudget(spent, budget)

        assertEquals(0.0, result, 0.001)
    }
}
