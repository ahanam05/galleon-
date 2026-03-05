package com.ahanam05.galleon

import com.ahanam05.galleon.data.models.Expense
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
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

        assertEquals("Thursday, December 25", result)
    }

    @Test
    fun getTotalAmount_withMultipleExpenses_returnsCorrectSum() {
        val expenses = listOf(
            Expense("1","Coffee", 9.50,"Food", 0L),
            Expense("2","Movie", 15.00,"Entertainment",  0L),
            Expense("3","Groceries", 75.5,"Food",  0L)
        )

        val result = getTotalAmount(expenses)

        assertEquals("100.0", result)
    }

    @Test
    fun getTotalAmount_withEmptyList_returnsZero() {
        val expenses = emptyList<Expense>()

        val result = getTotalAmount(expenses)

        assertEquals("0.0", result)
    }

    @Test
    fun getWeekStartDate_returnsStartingOnSunday() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)

        val result = Calendar.getInstance().apply { timeInMillis = getWeekStartDate(calendar.timeInMillis) }

        assertEquals(Calendar.SUNDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(2, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun getWeekEndDate_returnsEndingOnSaturday() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)

        val result = Calendar.getInstance().apply { timeInMillis = getWeekEndDate(calendar.timeInMillis) }

        assertEquals(Calendar.SATURDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(8, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun getWeekStartDate_whenAlreadySunday_returnsSameDay() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 2)

        val result = Calendar.getInstance().apply { timeInMillis = getWeekStartDate(calendar.timeInMillis) }

        assertEquals(Calendar.SUNDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(2, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun getWeekEndDate_whenAlreadySaturday_returnsSameDay() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 8)

        val result = Calendar.getInstance().apply { timeInMillis = getWeekEndDate(calendar.timeInMillis) }

        assertEquals(Calendar.SATURDAY, result.get(Calendar.DAY_OF_WEEK))
        assertEquals(8, result.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun getWeekRange_returnsCorrectPair() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 5)

        val (start, end) = getWeekRange(calendar.timeInMillis)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(Calendar.SUNDAY, startCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(2, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.SATURDAY, endCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(8, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun isInWeek_dateWithinWeek_returnsTrue() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 2)
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        calendar.set(2025, Calendar.FEBRUARY, 5)
        val dateToCheck = calendar.timeInMillis

        assertTrue(isInWeek(dateToCheck, weekStart, weekEnd))
    }

    @Test
    fun isInWeek_dateOutsideWeek_returnsFalse() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 2)
        val weekStart = getWeekStartDate(calendar.timeInMillis)
        val weekEnd = getWeekEndDate(calendar.timeInMillis)

        calendar.set(2025, Calendar.FEBRUARY, 9)
        val dateToCheck = calendar.timeInMillis

        assertFalse(isInWeek(dateToCheck, weekStart, weekEnd))
    }

    @Test
    fun formatWeekRange_sameMonth_returnsCorrectFormat() {
        val startCal = Calendar.getInstance()
        startCal.set(2025, Calendar.FEBRUARY, 2)
        val endCal = Calendar.getInstance()
        endCal.set(2025, Calendar.FEBRUARY, 8)

        val result = formatWeekRange(startCal.timeInMillis, endCal.timeInMillis)

        assertEquals("Feb 2 - 8", result)
    }

    @Test
    fun formatWeekRange_differentMonths_returnsCorrectFormat() {
        val startCal = Calendar.getInstance()
        startCal.set(2025, Calendar.JANUARY, 26)
        val endCal = Calendar.getInstance()
        endCal.set(2025, Calendar.FEBRUARY, 1)

        val result = formatWeekRange(startCal.timeInMillis, endCal.timeInMillis)

        assertEquals("Jan 26 - Feb 1", result)
    }

    @Test
    fun getWeekRange_acrossYearBoundary_returnsCorrectWeek() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.DECEMBER, 31)

        val (start, end) = getWeekRange(calendar.timeInMillis)
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        val endCal = Calendar.getInstance().apply { timeInMillis = end }

        assertEquals(Calendar.SUNDAY, startCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(28, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, startCal.get(Calendar.MONTH))
        assertEquals(Calendar.SATURDAY, endCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(3, endCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JANUARY, endCal.get(Calendar.MONTH))
    }

    @Test
    fun formatMonth_returnsCorrectlyFormattedString() {
        val calendar = Calendar.getInstance(Locale.US)
        calendar.set(2025, Calendar.MARCH, 15)
        val timestamp = calendar.timeInMillis

        val result = formatMonth(timestamp)

        assertEquals("March 2025", result)
    }

    @Test
    fun formatMonth_handlesDifferentMonths() {
        val calendar = Calendar.getInstance(Locale.US)
        calendar.set(2026, Calendar.DECEMBER, 1)
        val timestamp = calendar.timeInMillis

        val result = formatMonth(timestamp)

        assertEquals("December 2026", result)
    }

    @Test
    fun formatMonth_handlesYearBoundary() {
        val calendar = Calendar.getInstance(Locale.US)
        calendar.set(2025, Calendar.JANUARY, 1)
        val timestamp = calendar.timeInMillis

        val result = formatMonth(timestamp)

        assertEquals("January 2025", result)
    }

    @Test
    fun formatMonth_ignoresDayOfMonth() {
        val calendar1 = Calendar.getInstance(Locale.US)
        calendar1.set(2025, Calendar.FEBRUARY, 1)
        val calendar2 = Calendar.getInstance(Locale.US)
        calendar2.set(2025, Calendar.FEBRUARY, 28)

        val result1 = formatMonth(calendar1.timeInMillis)
        val result2 = formatMonth(calendar2.timeInMillis)

        assertEquals(result1, result2)
        assertEquals("February 2025", result1)
    }

    @Test
    fun getMonthStartDate_returnsFirstDayOfMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 15, 10, 30, 45)
        calendar.set(Calendar.MILLISECOND, 123)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthStartDate(calendar.timeInMillis) }

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, result.get(Calendar.MINUTE))
        assertEquals(0, result.get(Calendar.SECOND))
        assertEquals(0, result.get(Calendar.MILLISECOND))
    }

    @Test
    fun getMonthEndDate_returnsLastDayOfMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 15, 10, 30, 45)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(28, result.get(Calendar.DAY_OF_MONTH)) // February 2025 has 28 days
        assertEquals(23, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, result.get(Calendar.MINUTE))
        assertEquals(59, result.get(Calendar.SECOND))
    }

    @Test
    fun getMonthStartDate_whenAlreadyFirstDay_returnsSameDay() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthStartDate(calendar.timeInMillis) }

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, result.get(Calendar.MINUTE))
        assertEquals(0, result.get(Calendar.SECOND))
    }

    @Test
    fun getMonthEndDate_whenAlreadyLastDay_returnsSameDay() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 31, 23, 59, 59)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(31, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, result.get(Calendar.MINUTE))
        assertEquals(59, result.get(Calendar.SECOND))
    }

    @Test
    fun getMonthStartDate_handlesLeapYear() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthStartDate(calendar.timeInMillis) }

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.FEBRUARY, result.get(Calendar.MONTH))
        assertEquals(2024, result.get(Calendar.YEAR))
    }

    @Test
    fun getMonthEndDate_handlesLeapYear() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.FEBRUARY, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(29, result.get(Calendar.DAY_OF_MONTH)) // February 2024 has 29 days (leap year)
    }

    @Test
    fun getMonthStartDate_handlesDecember() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.DECEMBER, 25)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthStartDate(calendar.timeInMillis) }

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, result.get(Calendar.MONTH))
        assertEquals(2025, result.get(Calendar.YEAR))
    }

    @Test
    fun getMonthEndDate_handlesDecember() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.DECEMBER, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(31, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.DECEMBER, result.get(Calendar.MONTH))
    }

    @Test
    fun getMonthStartDate_handlesJanuary() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthStartDate(calendar.timeInMillis) }

        assertEquals(1, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JANUARY, result.get(Calendar.MONTH))
    }

    @Test
    fun getMonthEndDate_handlesJanuary() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.JANUARY, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(31, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.JANUARY, result.get(Calendar.MONTH))
    }

    @Test
    fun getMonthStartDate_handles30DayMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.APRIL, 15)

        val result = Calendar.getInstance().apply { timeInMillis = getMonthEndDate(calendar.timeInMillis) }

        assertEquals(30, result.get(Calendar.DAY_OF_MONTH))
    }
}
