package com.ahanam05.galleon.data.repository

import com.ahanam05.galleon.data.models.Expense

interface ExpenseRepository {
    fun addExpense(userId: String, expense: Expense): String?
    fun getAllExpenses(userId: String): List<Expense>
    fun getAllDailyExpenses(userId: String): List<Expense>
    fun getAllWeeklyExpenses(userId: String): List<Expense>
    fun getAllMonthlyExpenses(userId: String): List<Expense>
    fun getExpenseById(userId: String, expenseId: String): Expense?
    fun updateExpense(userId: String, expenseId: String, expense: Expense): String?
    fun deleteExpense(userId: String, expenseId: String): Boolean
}