package com.ahanam05.galleon.data.repository

import app.cash.turbine.test
import com.ahanam05.galleon.data.models.Budget
import com.ahanam05.galleon.data.models.Expense
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ExpenseRepositoryImplTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDocument: DocumentReference
    private lateinit var expensesCollection: CollectionReference
    private lateinit var expenseDocument: DocumentReference
    private lateinit var budgetsCollection: CollectionReference
    private lateinit var budgetDocument: DocumentReference

    private lateinit var repository: ExpenseRepositoryImpl
    private lateinit var mockListenerRegistration: ListenerRegistration

    private val testUserId = "testUserId"
    private val testExpenseId = "testExpenseId"

    @Before
    fun setUp() {
        firestore = mock()
        usersCollection = mock()
        userDocument = mock()
        expensesCollection = mock()
        expenseDocument = mock()
        budgetsCollection = mock()
        budgetDocument = mock()
        mockListenerRegistration = mock()

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document(any())).thenReturn(userDocument)
        whenever(userDocument.collection("expenses")).thenReturn(expensesCollection)
        whenever(expensesCollection.document(any())).thenReturn(expenseDocument)
        whenever(userDocument.collection("budgets")).thenReturn(budgetsCollection)
        whenever(budgetsCollection.document(any())).thenReturn(budgetDocument)

        repository = ExpenseRepositoryImpl(firestore)
    }

    @Test
    fun addExpense_callsFirestoreWithCorrectData() = runTest {
        val expense = Expense(
            id = testExpenseId,
            title = "Coffee",
            amount = 5.0,
            category = "Food",
            date = 123L
        )
        val mockDocumentReference: DocumentReference = mock()
        val addTask = Tasks.forResult(mockDocumentReference)
        whenever(expensesCollection.add(expense)).thenReturn(addTask)

        val result = repository.addExpense(testUserId, expense)

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).add(expense)
        assertEquals(testExpenseId, result)
    }

    @Test
    fun getAllExpenses_emitsListOfExpensesFromFirestore() = runTest {
        val expenses = listOf(
            Expense(id = "1", title = "Coffee", amount = 5.0, category = "Food", date = 123L),
            Expense(id = "2", title = "Bus", amount = 2.5, category = "Transport", date = 456L)
        )
        val mockSnapshot: QuerySnapshot = mock()
        whenever(mockSnapshot.toObjects(Expense::class.java)).thenReturn(expenses)
        val listenerCaptor = argumentCaptor<EventListener<QuerySnapshot>>()
        whenever(expensesCollection.addSnapshotListener(listenerCaptor.capture()))
            .thenReturn(mockListenerRegistration)

        repository.getAllExpenses(testUserId).test {
            listenerCaptor.firstValue.onEvent(mockSnapshot, null)

            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("Coffee", result[0].title)
            assertEquals("Bus", result[1].title)
            cancelAndIgnoreRemainingEvents()
        }

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).addSnapshotListener(any())
    }

    @Test
    fun getAllExpenses_emitsErrorWhenFirestoreFails() = runTest {
        val exception = mock<FirebaseFirestoreException>()
        val listenerCaptor = argumentCaptor<EventListener<QuerySnapshot>>()
        whenever(expensesCollection.addSnapshotListener(listenerCaptor.capture()))
            .thenReturn(mockListenerRegistration)

        repository.getAllExpenses(testUserId).test {
            listenerCaptor.firstValue.onEvent(null, exception)

            awaitError()
        }
    }

    @Test
    fun updateExpense_callsFirestoreUpdateWithCorrectData() = runTest {
        val expense = Expense(
            id = testExpenseId,
            title = "Updated Coffee",
            amount = 7.5,
            category = "Food",
            date = 789L
        )
        val updates = hashMapOf<String, Any>(
            "amount" to expense.amount,
            "category" to expense.category,
            "date" to expense.date,
            "title" to expense.title
        )
        val updateTask = Tasks.forResult<Void>(null)
        whenever(expenseDocument.update(updates)).thenReturn(updateTask)

        val result = repository.updateExpense(testUserId, testExpenseId, expense)

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).document(testExpenseId)
        verify(expenseDocument).update(updates)
        assertEquals(testExpenseId, result)
    }

    @Test
    fun deleteExpense_returnsTrueOnSuccessfulDeletion() = runTest {
        val deleteTask = Tasks.forResult<Void>(null)
        whenever(expenseDocument.delete()).thenReturn(deleteTask)

        val result = repository.deleteExpense(testUserId, testExpenseId)

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).document(testExpenseId)
        verify(expenseDocument).delete()
        assertTrue(result)
    }

    @Test
    fun deleteExpense_returnsFalseOnDeletionFailure() = runTest {
        val exception = Exception("Delete failed")
        val deleteTask = Tasks.forException<Void>(exception)
        whenever(expenseDocument.delete()).thenReturn(deleteTask)

        val result = repository.deleteExpense(testUserId, testExpenseId)

        assertFalse(result)
    }

    @Test
    fun setMonthlyBudget_returnsTrueOnSuccessfulSet() = runTest {
        val budget = Budget(monthlyBudget = 30000.0, monthYear = "2025-01")
        val setTask = Tasks.forResult<Void>(null)
        whenever(budgetDocument.set(budget)).thenReturn(setTask)

        val result = repository.setMonthlyBudget(testUserId, "2025-01", budget)

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("budgets")
        verify(budgetsCollection).document("2025-01")
        verify(budgetDocument).set(budget)
        assertTrue(result)
    }

    @Test
    fun setMonthlyBudget_callsFirestoreWithCorrectData() = runTest {
        val budget = Budget(monthlyBudget = 50000.0, monthYear = "2025-02")
        val setTask = Tasks.forResult<Void>(null)
        whenever(budgetDocument.set(budget)).thenReturn(setTask)

        repository.setMonthlyBudget(testUserId, "2025-02", budget)

        verify(budgetsCollection).document("2025-02")
        verify(budgetDocument).set(budget)
    }

    @Test
    fun setMonthlyBudget_returnsFalseOnFailure() = runTest {
        val budget = Budget(monthlyBudget = 30000.0, monthYear = "2025-01")
        val exception = Exception("Set failed")
        val setTask = Tasks.forException<Void>(exception)
        whenever(budgetDocument.set(budget)).thenReturn(setTask)

        val result = repository.setMonthlyBudget(testUserId, "2025-01", budget)

        assertFalse(result)
    }

    @Test
    fun getMonthlyBudget_returnsBudgetWhenExists() = runTest {
        val budget = Budget(monthlyBudget = 30000.0, monthYear = "2025-01")
        val mockDocumentSnapshot = mock<DocumentSnapshot>()
        val getTask = Tasks.forResult(mockDocumentSnapshot)
        whenever(budgetDocument.get()).thenReturn(getTask)
        whenever(mockDocumentSnapshot.toObject(Budget::class.java)).thenReturn(budget)

        val result = repository.getMonthlyBudget(testUserId, "2025-01")

        verify(firestore).collection("users")
        verify(usersCollection).document(testUserId)
        verify(userDocument).collection("budgets")
        verify(budgetsCollection).document("2025-01")
        verify(budgetDocument).get()
        assertEquals(30000.0, result?.monthlyBudget)
        assertEquals("2025-01", result?.monthYear)
    }

    @Test
    fun getMonthlyBudget_callsFirestoreWithCorrectPath() = runTest {
        val mockDocumentSnapshot = mock<DocumentSnapshot>()
        val getTask = Tasks.forResult(mockDocumentSnapshot)
        whenever(budgetDocument.get()).thenReturn(getTask)
        whenever(mockDocumentSnapshot.toObject(Budget::class.java)).thenReturn(null)

        repository.getMonthlyBudget(testUserId, "2025-03")

        verify(budgetsCollection).document("2025-03")
        verify(budgetDocument).get()
    }

    @Test
    fun getMonthlyBudget_returnsNullWhenBudgetDoesNotExist() = runTest {
        val mockDocumentSnapshot = mock<DocumentSnapshot>()
        val getTask = Tasks.forResult(mockDocumentSnapshot)
        whenever(budgetDocument.get()).thenReturn(getTask)
        whenever(mockDocumentSnapshot.toObject(Budget::class.java)).thenReturn(null)

        val result = repository.getMonthlyBudget(testUserId, "2025-01")

        assertEquals(null, result)
    }

    @Test
    fun getMonthlyBudget_returnsNullOnFailure() = runTest {
        val exception = Exception("Get failed")
        val getTask = Tasks.forException<DocumentSnapshot>(exception)
        whenever(budgetDocument.get()).thenReturn(getTask)

        val result = repository.getMonthlyBudget(testUserId, "2025-01")

        assertEquals(null, result)
    }
}
