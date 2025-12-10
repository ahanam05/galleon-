package com.ahanam05.galleon.data.repository

import com.ahanam05.galleon.data.models.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(userId: String, expense: Expense): String?
    suspend fun getAllExpenses(userId: String): Flow<List<Expense>>
    suspend fun getAllDailyExpenses(userId: String): List<Expense>
    suspend fun getAllWeeklyExpenses(userId: String): List<Expense>
    suspend fun getAllMonthlyExpenses(userId: String): List<Expense>
    suspend fun getExpenseById(userId: String, expenseId: String): Expense?
    suspend fun updateExpense(userId: String, expenseId: String, expense: Expense): String?
    suspend fun deleteExpense(userId: String, expenseId: String): Boolean
}