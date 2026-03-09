package com.ahanam05.galleon.home

import app.cash.turbine.test
import com.ahanam05.galleon.data.aggregator.ExpenseAggregator
import com.ahanam05.galleon.data.models.Budget
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.data.repository.ExpenseRepository
import com.ahanam05.galleon.getMonthEndDate
import com.ahanam05.galleon.getMonthStartDate
import com.ahanam05.galleon.getWeekEndDate
import com.ahanam05.galleon.getWeekStartDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Calendar

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private lateinit var mockExpenseRepository: ExpenseRepository
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testUserId = "testUser"
    private val testExpenses = listOf(
        Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = 1672531200000),
        Expense(id = "2", title = "Bus Fare", amount = 2.5, category = "Transportation", date = 1672617600000)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockExpenseRepository = mock()
        mockFirebaseAuth = mock()
        mockFirebaseUser = mock()

        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(testUserId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsExpensesCorrectly() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(testExpenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        viewModel.expenses.test {
            val emitted = awaitItem()
            assertEquals(testExpenses, emitted)
            cancelAndIgnoreRemainingEvents()
        }
        verify(mockExpenseRepository).getAllExpenses(testUserId)
    }

    @Test
    fun addExpense_callsRepositoryWithCorrectExpense() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        val title = "Coffee"
        val category = "Food"
        val amountStr = "3.50"
        val date = 123456L
        val expectedExpense = Expense(
            id = "",
            title = title,
            category = category,
            amount = 3.50,
            date = date
        )

        viewModel.addExpense(title, category, amountStr, date)
        advanceUntilIdle()

        verify(mockExpenseRepository).addExpense(testUserId, expectedExpense)
    }

    @Test
    fun updateExpense_callsRepositoryWithUpdateExpense() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        val existing = Expense(id = "42", title = "Old", amount = 10.0, category = "Misc", date = 111111L)
        val updatedTitle = "Deluxe Lunch"
        val updatedCategory = "Food"
        val updatedAmountStr = "25.0"
        val updatedDate = existing.date
        val expectedExpense = Expense(
            id = existing.id,
            title = updatedTitle,
            category = updatedCategory,
            amount = 25.0,
            date = updatedDate
        )

        viewModel.updateExpense(existing, updatedTitle, updatedCategory, updatedAmountStr, updatedDate)
        advanceUntilIdle()

        verify(mockExpenseRepository).updateExpense(testUserId, existing.id, expectedExpense)
    }

    @Test
    fun deleteExpense_callsRepositoryCorrectly() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        val expenseId = "123"

        viewModel.deleteExpense(expenseId)
        advanceUntilIdle()

        verify(mockExpenseRepository).deleteExpense(testUserId, expenseId)
    }

    @Test
    fun init_initializesWeekBoundariesCorrectly() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        val currentTimeMillis = System.currentTimeMillis()
        val expectedWeekStart = getWeekStartDate(currentTimeMillis)
        val expectedWeekEnd = getWeekEndDate(currentTimeMillis)
        viewModel.weekStartDate.test {
            val weekStart = awaitItem()
            assertEquals(expectedWeekStart, weekStart)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.weekEndDate.test {
            val weekEnd = awaitItem()
            assertEquals(expectedWeekEnd, weekEnd)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSelectedDate_updatesWeekBoundaries() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        val testDate = 1705276800000L // Jan 15, 2024, 00:00:00 UTC
        val expectedWeekStart = getWeekStartDate(testDate)
        val expectedWeekEnd = getWeekEndDate(testDate)

        viewModel.updateSelectedDate(testDate)
        advanceUntilIdle()

        viewModel.weekStartDate.test {
            assertEquals(expectedWeekStart, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.weekEndDate.test {
            assertEquals(expectedWeekEnd, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun weeklyTotal_calculatesCorrectlyForWeekExpenses() = runTest {
        val weekStart = 1704585600000L // Jan 7, 2024 (Sunday) 00:00:00
        val weekEnd = 1705190399000L   // Jan 13, 2024 (Saturday) 23:59:59
        val expensesInWeek = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = weekStart + 86400000), // Monday
            Expense(id = "2", title = "Coffee", amount = 3.5, category = "Food", date = weekStart + 172800000), // Tuesday
            Expense(id = "3", title = "Metro", amount = 40.0, category = "Transportation", date = weekStart + 259200000) // Wednesday
        )
        val expensesOutsideWeek = listOf(
            Expense(id = "4", title = "Dinner", amount = 25.0, category = "Food", date = weekEnd + 86400000) // Next week
        )
        val allExpenses = expensesInWeek + expensesOutsideWeek
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(allExpenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.weeklyTotal.test {
            val total = awaitItem()
            assertEquals(58.5, total, 0.01) // 15.0 + 3.5 + 40.0
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dailyBreakdownByDay_computesCorrectBreakdown() = runTest {
        val weekStart = 1704585600000L // Jan 7, 2024 (Sunday)
        val monday = weekStart + 86400000L
        val tuesday = weekStart + 172800000L
        val expenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday),
            Expense(id = "2", title = "Coffee", amount = 3.5, category = "Food", date = monday),
            Expense(id = "3", title = "Gas", amount = 40.0, category = "Transportation", date = tuesday)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.dailyBreakdownByDay.test {
            val breakdown = awaitItem()
            assertEquals(7, breakdown.size)
            val mondayLabel = ExpenseAggregator.getDayLabel(monday)
            val tuesdayLabel = ExpenseAggregator.getDayLabel(tuesday)
            breakdown[mondayLabel]?.let { assertEquals(18.5, it, 0.01) } // 15.0 + 3.5
            breakdown[tuesdayLabel]?.let { assertEquals(40.0, it, 0.01) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dailyAverageAmount_calculatesCorrectlyForDaysWithExpenses() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val tuesday = weekStart + 172800000L
        val expenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 20.0, category = "Food", date = monday),
            Expense(id = "2", title = "Metro", amount = 40.0, category = "Transportation", date = tuesday)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.dailyAverageAmount.test {
            val average = awaitItem()
            assertEquals(30.0, average, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dailyAverageAmount_returnsZeroForEmptyWeek() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        viewModel.dailyAverageAmount.test {
            val average = awaitItem()
            assertEquals(0.0, average, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addExpense_recomputesWeeklyAggregates() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val initialExpenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday)
        )
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()
        viewModel.weeklyTotal.test {
            assertEquals(15.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }

        val newExpense = Expense(id = "2", title = "Coffee", amount = 3.5, category = "Food", date = monday)
        val updatedExpenses = initialExpenses + newExpense
        viewModel.addExpense("Coffee", "Food", "3.5", monday)
        advanceUntilIdle()
        expensesFlow.value = updatedExpenses
        advanceUntilIdle()

        viewModel.weeklyTotal.test {
            assertEquals(18.5, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateExpense_recomputesWeeklyAggregates() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val existingExpense = Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday)
        val initialExpenses = listOf(existingExpense)
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        val updatedExpense = existingExpense.copy(amount = 25.0)
        viewModel.updateExpense(existingExpense, "Lunch", "Food", "25.0", monday)
        advanceUntilIdle()
        expensesFlow.value = listOf(updatedExpense)
        advanceUntilIdle()

        viewModel.weeklyTotal.test {
            assertEquals(25.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteExpense_recomputesWeeklyAggregates() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val expense1 = Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday)
        val expense2 = Expense(id = "2", title = "Coffee", amount = 3.5, category = "Food", date = monday)
        val initialExpenses = listOf(expense1, expense2)
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()
        viewModel.weeklyTotal.test {
            assertEquals(18.5, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.deleteExpense("2")
        advanceUntilIdle()
        expensesFlow.value = listOf(expense1)
        advanceUntilIdle()

        viewModel.weeklyTotal.test {
            assertEquals(15.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSelectedTab_triggersRecomputation() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val expenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.updateSelectedTab("Weekly")
        advanceUntilIdle()

        viewModel.weeklyTotal.test {
            assertEquals(15.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun init_initializesMonthBoundariesCorrectly() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        val currentTimeMillis = System.currentTimeMillis()
        val expectedMonthStart = getMonthStartDate(currentTimeMillis)
        val expectedMonthEnd = getMonthEndDate(currentTimeMillis)
        viewModel.monthStartDate.test {
            val monthStart = awaitItem()
            assertEquals(expectedMonthStart, monthStart)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.monthEndDate.test {
            val monthEnd = awaitItem()
            assertEquals(expectedMonthEnd, monthEnd)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyTotal_calculatesCorrectlyForMonthExpenses() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis
        val expensesInMonth = listOf(
            Expense(id = "1", title = "Groceries", amount = 100.0, category = "Food", date = monthStart + 86400000),
            Expense(id = "2", title = "Rent", amount = 1000.0, category = "Housing", date = monthStart + 172800000),
            Expense(id = "3", title = "Utilities", amount = 150.0, category = "Bills", date = monthStart + 259200000)
        )
        calendar.add(Calendar.MONTH, 1)
        val nextMonthExpense = Expense(id = "4", title = "Coffee", amount = 5.0, category = "Food", date = calendar.timeInMillis)
        val allExpenses = expensesInMonth + nextMonthExpense
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(allExpenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(monthStart)
        advanceUntilIdle()

        viewModel.monthlyTotal.test {
            val total = awaitItem()
            assertEquals(1250.0, total, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyComparison_showsIncreaseCorrectly() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.FEBRUARY, 15)
        val currentDate = calendar.timeInMillis
        val currentMonthStart = getMonthStartDate(currentDate)
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = getMonthStartDate(calendar.timeInMillis)
        val expenses = listOf(
            Expense(id = "1", title = "Current", amount = 1200.0, category = "Food", date = currentMonthStart + 86400000),
            Expense(id = "2", title = "Previous", amount = 1000.0, category = "Food", date = previousMonthStart + 86400000)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()

        viewModel.monthlyComparison.test {
            val comparison = awaitItem()
            assertEquals("20% more than last month", comparison.first)
            assertEquals(true, comparison.second)
            assertEquals(true, comparison.third)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyComparison_showsDecreaseCorrectly() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        val currentDate = calendar.timeInMillis
        val currentMonthStart = getMonthStartDate(currentDate)
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = getMonthStartDate(calendar.timeInMillis)
        val expenses = listOf(
            Expense(id = "1", title = "Current", amount = 800.0, category = "Food", date = currentMonthStart + 86400000),
            Expense(id = "2", title = "Previous", amount = 1000.0, category = "Food", date = previousMonthStart + 86400000)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()

        viewModel.monthlyComparison.test {
            val comparison = awaitItem()
            assertEquals("20% less than last month", comparison.first)
            assertEquals(false, comparison.second)
            assertEquals(true, comparison.third)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyComparison_hidesWhenCurrentMonthIsZero() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        val currentDate = calendar.timeInMillis
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = getMonthStartDate(calendar.timeInMillis)
        val expenses = listOf(
            Expense(id = "1", title = "Previous", amount = 1000.0, category = "Food", date = previousMonthStart + 86400000)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()

        viewModel.monthlyComparison.test {
            val comparison = awaitItem()
            assertEquals(false, comparison.third)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyComparison_hidesWhenPreviousMonthIsZero() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        val currentDate = calendar.timeInMillis
        val currentMonthStart = getMonthStartDate(currentDate)
        val expenses = listOf(
            Expense(id = "1", title = "Current", amount = 1000.0, category = "Food", date = currentMonthStart + 86400000)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()

        viewModel.monthlyComparison.test {
            val comparison = awaitItem()
            assertEquals(false, comparison.third)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun monthlyComparison_hidesWhenChangeLessThanOnePercent() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        val currentDate = calendar.timeInMillis
        val currentMonthStart = getMonthStartDate(currentDate)
        calendar.add(Calendar.MONTH, -1)
        val previousMonthStart = getMonthStartDate(calendar.timeInMillis)
        val expenses = listOf(
            Expense(id = "1", title = "Current", amount = 1004.0, category = "Food", date = currentMonthStart + 86400000),
            Expense(id = "2", title = "Previous", amount = 1000.0, category = "Food", date = previousMonthStart + 86400000)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()

        viewModel.monthlyComparison.test {
            val comparison = awaitItem()
            assertEquals(false, comparison.third)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addExpense_recomputesMonthlyAggregates() = runTest {
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        val currentDate = calendar.timeInMillis
        val currentMonthStart = getMonthStartDate(currentDate)
        val initialExpenses = listOf(
            Expense(id = "1", title = "Groceries", amount = 100.0, category = "Food", date = currentMonthStart + 86400000)
        )
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(currentDate)
        advanceUntilIdle()
        viewModel.monthlyTotal.test {
            assertEquals(100.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
        val newExpense = Expense(id = "2", title = "Rent", amount = 1000.0, category = "Housing", date = currentMonthStart + 172800000)
        val updatedExpenses = initialExpenses + newExpense

        viewModel.addExpense("Rent", "Housing", "1000.0", currentMonthStart + 172800000)
        advanceUntilIdle()
        expensesFlow.value = updatedExpenses
        advanceUntilIdle()

        viewModel.monthlyTotal.test {
            assertEquals(1100.0, awaitItem(), 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setMonthlyBudget_callsRepositoryCorrectly() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        val testBudget = Budget(monthYear = "2025-03", monthlyBudget = 15000.0)
        whenever(mockExpenseRepository.setMonthlyBudget(any(), any(), any())).thenReturn(true)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        viewModel.updateSelectedDate(calendar.timeInMillis)
        advanceUntilIdle()

        viewModel.setMonthlyBudget(testBudget.monthYear, testBudget)
        advanceUntilIdle()

        verify(mockExpenseRepository).setMonthlyBudget(
            eq(testUserId),
            eq("2025-03"),
            any()
        )
    }

    @Test
    fun fetchMonthlyBudget_retrievesBudgetFromRepository() = runTest {
        val testBudget = Budget(monthYear = "2025-03", monthlyBudget = 15000.0)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        whenever(mockExpenseRepository.getMonthlyBudget(testUserId, "2025-03")).thenReturn(testBudget)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 15)
        viewModel.updateSelectedDate(calendar.timeInMillis)
        advanceUntilIdle()

        viewModel.monthlyBudget.test {
            val budget = awaitItem()
            assertEquals(testBudget, budget)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun fetchMonthlyBudget_handlesNullBudget() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        whenever(mockExpenseRepository.getMonthlyBudget(eq(testUserId), any())).thenReturn(null)

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        viewModel.monthlyBudget.test {
            val budget = awaitItem()
            assertEquals(null, budget)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSelectedDate_fetchesBudgetForNewMonth() = runTest {
        val marchBudget = Budget(monthYear = "2025-03", monthlyBudget = 15000.0)
        val aprilBudget = Budget(monthYear = "2025-04", monthlyBudget = 18000.0)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))
        whenever(mockExpenseRepository.getMonthlyBudget(testUserId, "2025-03")).thenReturn(marchBudget)
        whenever(mockExpenseRepository.getMonthlyBudget(testUserId, "2025-04")).thenReturn(aprilBudget)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        val marchCal = Calendar.getInstance()
        marchCal.set(2025, Calendar.MARCH, 15)
        viewModel.updateSelectedDate(marchCal.timeInMillis)
        advanceUntilIdle()
        val aprilCal = Calendar.getInstance()
        aprilCal.set(2025, Calendar.APRIL, 15)
        viewModel.updateSelectedDate(aprilCal.timeInMillis)
        advanceUntilIdle()

        verify(mockExpenseRepository).getMonthlyBudget(testUserId, "2025-03")
        verify(mockExpenseRepository).getMonthlyBudget(testUserId, "2025-04")
    }

    @Test
    fun topCategory_returnsNullForEmptyWeek() = runTest {
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(emptyList()))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals(null, category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun topCategory_identifiesCategoryWithHighestSpending() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val tuesday = weekStart + 172800000L
        val expenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday),
            Expense(id = "2", title = "Coffee", amount = 5.0, category = "Food", date = monday),
            Expense(id = "3", title = "Metro", amount = 10.0, category = "Transportation", date = tuesday),
            Expense(id = "4", title = "Movie", amount = 12.0, category = "Entertainment", date = tuesday)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals("Food", category?.first)
            assertEquals(48.0, category?.second ?: 0.0, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun topCategory_updatesWhenExpensesAdded() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val initialExpenses = listOf(
            Expense(id = "1", title = "Coffee", amount = 5.0, category = "Food", date = monday)
        )
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()
        viewModel.topCategory.test {
            assertEquals("Food", awaitItem()?.first)
            cancelAndIgnoreRemainingEvents()
        }

        val newExpense = Expense(id = "2", title = "Metro", amount = 20.0, category = "Transportation", date = monday)
        val updatedExpenses = initialExpenses + newExpense
        viewModel.addExpense("Metro", "Transportation", "20.0", monday)
        advanceUntilIdle()
        expensesFlow.value = updatedExpenses
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals("Transportation", category?.first)
            assertEquals(80.0, category?.second ?: 0.0, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun topCategory_updatesWhenExpensesDeleted() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val expense1 = Expense(id = "1", title = "Rent", amount = 1000.0, category = "Housing", date = monday)
        val expense2 = Expense(id = "2", title = "Groceries", amount = 100.0, category = "Food", date = monday)
        val initialExpenses = listOf(expense1, expense2)
        val expensesFlow = MutableStateFlow(initialExpenses)
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(expensesFlow)
        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()
        viewModel.topCategory.test {
            assertEquals("Housing", awaitItem()?.first)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.deleteExpense("1")
        advanceUntilIdle()
        expensesFlow.value = listOf(expense2)
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals("Food", category?.first)
            assertEquals(100.0, category?.second ?: 0.0, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun topCategory_onlyConsidersExpensesInCurrentWeek() = runTest {
        val weekStart = 1704585600000L
        val weekEnd = 1705190399000L
        val monday = weekStart + 86400000L
        val nextWeek = weekEnd + 86400001L
        val expenses = listOf(
            Expense(id = "1", title = "Coffee", amount = 5.0, category = "Food", date = monday),
            Expense(id = "2", title = "Expensive Dinner", amount = 200.0, category = "Food", date = nextWeek)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals("Food", category?.first)
            assertEquals(100.0, category?.second ?: 0.0, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun topCategory_handlesTieByReturningFirst() = runTest {
        val weekStart = 1704585600000L
        val monday = weekStart + 86400000L
        val expenses = listOf(
            Expense(id = "1", title = "Lunch", amount = 15.0, category = "Food", date = monday),
            Expense(id = "2", title = "Metro", amount = 15.0, category = "Transportation", date = monday)
        )
        whenever(mockExpenseRepository.getAllExpenses(testUserId)).thenReturn(flowOf(expenses))

        viewModel = HomeViewModel(mockExpenseRepository, mockFirebaseAuth)
        advanceUntilIdle()
        viewModel.updateSelectedDate(weekStart)
        advanceUntilIdle()

        viewModel.topCategory.test {
            val category = awaitItem()
            assertEquals(50.0, category?.second ?: 0.0, 0.01)
            // Either "Food" or "Transportation" is acceptable for a tie
            cancelAndIgnoreRemainingEvents()
        }
    }
}
