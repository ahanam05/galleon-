package com.ahanam05.galleon.data.repository

import com.ahanam05.galleon.data.models.Expense
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ExpenseRepositoryImplTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var userDocument: DocumentReference
    private lateinit var expensesCollection: CollectionReference
    private lateinit var repository: ExpenseRepositoryImpl
    private lateinit var mockListenerRegistration: ListenerRegistration
    private lateinit var mockQuery: Query
    private lateinit var mockSnapshot: QuerySnapshot


    @Before
    fun setUp() {
        firestore = mock()
        usersCollection = mock()
        userDocument = mock()
        expensesCollection = mock()
        mockListenerRegistration = mock()
        mockQuery = mock()
        mockSnapshot = mock()

        whenever(firestore.collection("users")).thenReturn(usersCollection)
        whenever(usersCollection.document(any())).thenReturn(userDocument)
        whenever(userDocument.collection("expenses")).thenReturn(expensesCollection)
        whenever(expensesCollection.addSnapshotListener(any())).thenReturn(mockListenerRegistration)

        repository = ExpenseRepositoryImpl(firestore)
    }

    @Test
    fun addExpense_callsCorrectCollection() = runTest {
        val userId = "testUserId"
        val expense = Expense(title = "Coffee", amount = 5.0, category = "Food", date = 123L)
        val mockTask: Task<DocumentReference> = mock()
        whenever(expensesCollection.add(expense)).thenReturn(mockTask)

        repository.addExpense(userId, expense)

        verify(firestore).collection("users")
        verify(usersCollection).document(userId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).add(expense)
    }

    @Test
    fun getAllExpenses_callsCorrectCollection() = runTest {
        val userId = "testUserId"
        val mockSnapshot: QuerySnapshot = mock()
        whenever(expensesCollection.addSnapshotListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument< (QuerySnapshot?, Exception?) -> Unit>(0)
            listener.invoke(mockSnapshot, null)
            mockListenerRegistration
        }

        repository.getAllExpenses(userId).first()

        verify(firestore).collection("users")
        verify(usersCollection).document(userId)
        verify(userDocument).collection("expenses")
        verify(expensesCollection).addSnapshotListener(any())
    }
}
