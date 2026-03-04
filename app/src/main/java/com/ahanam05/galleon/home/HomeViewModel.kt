package com.ahanam05.galleon.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahanam05.galleon.data.aggregator.ExpenseAggregator
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.data.repository.ExpenseRepository
import com.ahanam05.galleon.getWeekEndDate
import com.ahanam05.galleon.getWeekStartDate
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: MutableStateFlow<List<Expense>> = _expenses

    private val _weekStartDate = MutableStateFlow<Long>(getWeekStartDate(Calendar.getInstance().timeInMillis))
    val weekStartDate: StateFlow<Long> = _weekStartDate.asStateFlow()

    private val _weekEndDate = MutableStateFlow<Long>(getWeekEndDate(Calendar.getInstance().timeInMillis))
    val weekEndDate: StateFlow<Long> = _weekEndDate.asStateFlow()

    private val _weeklyTotal = MutableStateFlow<Double>(0.0)
    val weeklyTotal: StateFlow<Double> = _weeklyTotal.asStateFlow()

    private val _dailyBreakdownByDay = MutableStateFlow<Map<String, Double>>(emptyMap())
    val dailyBreakdownByDay: StateFlow<Map<String, Double>> = _dailyBreakdownByDay.asStateFlow()

    private val _dailyAverageAmount = MutableStateFlow<Double>(0.0)
    val dailyAverageAmount: StateFlow<Double> = _dailyAverageAmount.asStateFlow()

    private val _topCategory = MutableStateFlow<Pair<String, Double>?>(null)
    val topCategory: StateFlow<Pair<String, Double>?> = _topCategory.asStateFlow()

    init {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                expenseRepository.getAllExpenses(userId).collect { expenseList ->
                    _expenses.value = expenseList
                    recomputeWeeklyAggregates()
                }
            }
        }
    }

    fun addExpense(title: String, category: String, amountStr: String, date: Long) {
        val userId = auth.currentUser?.uid ?: return
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val expense = Expense(
            title = title,
            amount = amount,
            category = category,
            date = date
        )

        viewModelScope.launch {
            expenseRepository.addExpense(userId, expense)
        }
    }

    fun updateExpense(existingExpense: Expense?, title: String, category: String, amountStr: String, date: Long){
        val userId = auth.currentUser?.uid ?: return
        val expenseId = existingExpense?.id?: ""
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val newExpense = Expense(
            id = expenseId,
            title = title,
            amount = amount,
            category = category,
            date = date
        )

        if(existingExpense == newExpense){
            Log.d("Update Expense", "No changes made to the expense")
            return
        }

        viewModelScope.launch {
            expenseRepository.updateExpense(userId, expenseId, newExpense)
        }
    }

    fun deleteExpense(expenseId: String){
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            expenseRepository.deleteExpense(userId, expenseId)
        }
    }

    fun updateSelectedDate(newDate: Long) {
        val newWeekStart = getWeekStartDate(newDate)
        val newWeekEnd = getWeekEndDate(newDate)

        _weekStartDate.value = newWeekStart
        _weekEndDate.value = newWeekEnd

        recomputeWeeklyAggregates()
    }

    fun updateSelectedTab(newTab: String) {
        recomputeWeeklyAggregates()
    }

    private fun recomputeWeeklyAggregates() {
        val currentExpenses = _expenses.value
        val weekStart = _weekStartDate.value
        val weekEnd = _weekEndDate.value

        val weekExpenses = currentExpenses.filter { expense ->
            expense.date in weekStart..weekEnd
        }

        val total = ExpenseAggregator.getWeeklyTotal(weekExpenses, weekStart, weekEnd)
        _weeklyTotal.value = total

        val breakdown = ExpenseAggregator.getWeeklyDailyBreakdown(weekExpenses, weekStart, weekEnd)
        _dailyBreakdownByDay.value = breakdown

        val daysWithExpenses = breakdown.values.count { it > 0.0 }
        val average = if (daysWithExpenses > 0) total / daysWithExpenses else 0.0
        _dailyAverageAmount.value = average

        val topCategory = ExpenseAggregator.getTopCategoryByExpense(currentExpenses, weekStart, weekEnd)
        _topCategory.value = topCategory

    }
}
