package com.ahanam05.galleon.home

import app.cash.turbine.test
import com.ahanam05.galleon.data.aggregator.ExpenseAggregator
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.data.repository.ExpenseRepository
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
}
