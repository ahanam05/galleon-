package com.ahanam05.galleon.data.repository

import com.ahanam05.galleon.data.models.Expense
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor (private val db: FirebaseFirestore) : ExpenseRepository{

    val USER_COLLECTION = "users"
    val EXPENSES_SUBCOLLECTION = "expenses"

    override suspend fun addExpense(
        userId: String,
        expense: Expense
    ): String? {
        db.collection(USER_COLLECTION)
            .document(userId)
            .collection(EXPENSES_SUBCOLLECTION)
            .add(expense)
            .await()

        return expense.id
    }

    override suspend fun getAllExpenses(userId: String): Flow<List<Expense>> = callbackFlow {
        val collection = db.collection(USER_COLLECTION).document(userId).collection(EXPENSES_SUBCOLLECTION)

        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val expenses = snapshot.toObjects(Expense::class.java)
                trySend(expenses)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getAllDailyExpenses(userId: String): List<Expense> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllWeeklyExpenses(userId: String): List<Expense> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllMonthlyExpenses(userId: String): List<Expense> {
        TODO("Not yet implemented")
    }

    override suspend fun getExpenseById(
        userId: String,
        expenseId: String
    ): Expense? {
        TODO("Not yet implemented")
    }

    override suspend fun updateExpense(
        userId: String,
        expenseId: String,
        expense: Expense
    ): String? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteExpense(
        userId: String,
        expenseId: String
    ): Boolean {
        TODO("Not yet implemented")
    }
}