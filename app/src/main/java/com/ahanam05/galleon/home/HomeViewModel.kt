package com.ahanam05.galleon.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahanam05.galleon.data.models.Expense
import com.ahanam05.galleon.data.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: MutableStateFlow<List<Expense>> = _expenses

    init {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                expenseRepository.getAllExpenses(userId).collect { expenseList ->
                    _expenses.value = expenseList
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
}
