package com.ahanam05.galleon.data.aggregator

import com.ahanam05.galleon.data.models.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.round

/**
 * Aggregates expense data for weekly/monthly views.
 * All functions are pure and operate on in-memory expense lists.
 */
object ExpenseAggregator {

    /**
     * Get the total spending for a given week.
     * @param expenses List of all expenses
     * @param weekStartMillis Start of week (Sunday, 00:00:00)
     * @param weekEndMillis End of week (Saturday, 23:59:59)
     * @return Total amount spent in the week
     */
    fun getWeeklyTotal(
        expenses: List<Expense>,
        weekStartMillis: Long,
        weekEndMillis: Long
    ): Double {
        return expenses
            .filter { it.date in weekStartMillis..weekEndMillis }
            .sumOf { it.amount }
    }

    /**
     * Get the total spending for a specific day.
     * @param expenses List of all expenses
     * @param dateMillis The date to check (any time on that day)
     * @return Total amount spent on that day
     */
    fun getDailyTotalForDate(expenses: List<Expense>, dateMillis: Long): Double {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        return expenses
            .filter { expense ->
                val expenseCal = Calendar.getInstance().apply { timeInMillis = expense.date }
                expenseCal.get(Calendar.YEAR) == year &&
                        expenseCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
            }
            .sumOf { it.amount }
    }

    /**
     * Get daily breakdown for the entire week.
     * Returns a Map of day labels to totals, e.g.:
     * "SUN, DEC 8" → 250.0
     * "MON, DEC 9" → 185.0
     *
     * @param expenses List of all expenses
     * @param weekStartMillis Start of week (Sunday)
     * @param weekEndMillis End of week (Saturday)
     * @return Map<String, Double> with entries for each day of the week
     */
    fun getWeeklyDailyBreakdown(
        expenses: List<Expense>,
        weekStartMillis: Long,
        weekEndMillis: Long
    ): Map<String, Double> {
        val breakdown = mutableMapOf<String, Double>()
        val calendar = Calendar.getInstance().apply { timeInMillis = weekStartMillis }

        repeat(7) {
            val dayLabel = getDayLabel(calendar.timeInMillis)
            val dayTotal = getDailyTotalForDate(expenses, calendar.timeInMillis)
            breakdown[dayLabel] = dayTotal

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return breakdown
    }

    /**
     * Calculate average daily spending for the week.
     * @param weeklyTotal Total spending for the week
     * @return Average per day (total / 7), rounded to 2 decimal places
     */
    fun getWeeklyAveragePerDay(weeklyTotal: Double): Double {
        val average = weeklyTotal / 7
        return round(average * 100) / 100
    }

    /**
     * Format a date as "MON, DEC 9" for breakdown display.
     * @param dateMillis The date to format
     * @return Formatted string like "MON, DEC 9"
     */
    fun getDayLabel(dateMillis: Long): String {
        val formatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        return formatter.format(Calendar.getInstance().apply { timeInMillis = dateMillis }.time)
            .uppercase()
    }

    /**
     * Find the top spending category for a week.
     * @param expenses List of all expenses
     * @param weekStartMillis Start of week
     * @param weekEndMillis End of week
     * @return Pair of (category name, percentage of total), e.g., ("Food", 65.0)
     */
    fun getTopCategoryByExpense(
        expenses: List<Expense>,
        weekStartMillis: Long,
        weekEndMillis: Long
    ): Pair<String, Double>? {
        val weekExpenses = expenses.filter { it.date in weekStartMillis..weekEndMillis }
        if (weekExpenses.isEmpty()) return null

        val weeklyTotal = getWeeklyTotal(expenses, weekStartMillis, weekEndMillis)
        if (weeklyTotal == 0.0) return null

        val categoryTotals = weekExpenses
            .groupBy { it.category }
            .mapValues { (_, categoryExpenses) -> categoryExpenses.sumOf { it.amount } }

        val topCategory = categoryTotals.maxByOrNull { it.value } ?: return null
        val percentage = round((topCategory.value / weeklyTotal) * 100)

        return Pair(topCategory.key, percentage)
    }

    /**
     * Get expenses for a specific day.
     * @param expenses List of all expenses
     * @param dateMillis The date to filter for
     * @return List of expenses on that day
     */
    fun getExpensesForDay(expenses: List<Expense>, dateMillis: Long): List<Expense> {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        return expenses.filter { expense ->
            val expenseCal = Calendar.getInstance().apply { timeInMillis = expense.date }
            expenseCal.get(Calendar.YEAR) == year &&
                    expenseCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
        }
    }

    /**
     * Get spending by day of week (for chart rendering).
     * Returns amounts in order: Sun, Mon, Tue, Wed, Thu, Fri, Sat
     *
     * @param breakdown Map from getDailyTotalForWeek
     * @return List of 7 doubles (Sun-Sat amounts)
     */
    fun getWeeklyChartData(breakdown: Map<String, Double>): List<Double> {
        return breakdown.values.toList()
    }
}
