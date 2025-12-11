package com.ahanam05.galleon.home

import app.cash.turbine.test
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.data.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
}
