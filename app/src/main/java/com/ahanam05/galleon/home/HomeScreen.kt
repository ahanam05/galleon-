package com.ahanam05.galleon.home

import android.util.Log
import java.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahanam05.galleon.home.components.AddExpenseModal
import com.ahanam05.galleon.home.components.DateNavigationRow
import com.ahanam05.galleon.home.components.DatePickerModal
import com.ahanam05.galleon.home.components.ExpenseCard
import com.ahanam05.galleon.home.components.NavigationDrawerContent
import com.ahanam05.galleon.home.components.TimePeriodTabs
import com.ahanam05.galleon.home.components.TopBar
import com.ahanam05.galleon.isSameDay
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import com.ahanam05.galleon.R
import com.ahanam05.galleon.getTotalAmount

data class ExpenseItem(
    val title: String,
    val category: String,
    val amount: String,
    val date: Long,
    val categoryColor: Color = Color(0xFFF7E8CA)
)

object Modes{
    const val DAILY = "Daily"
    const val WEEKLY = "Weekly"
    const val MONTHLY = "Monthly"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSignOutClick: () -> Unit, user: FirebaseUser?) {
    val expenses = remember {
        val today = Calendar.getInstance().timeInMillis
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, +1) }.timeInMillis
        listOf(
            ExpenseItem("Coffee", "Food", "200.50", today),
            ExpenseItem("Groceries", "Food",  "580", yesterday),
            ExpenseItem("Bus Fare", "Transportation",  "40", tomorrow),
            ExpenseItem("Dinner", "Food",  "380", today),
            ExpenseItem("Snacks", "Food", "120", yesterday),
            ExpenseItem("Movie Ticket", "Entertainment",  "285", today),
            ExpenseItem("Taxi", "Transportation",  "150", today)
        )
    }

    var selectedTab by remember { mutableStateOf(Modes.DAILY) }
    var selectedDate by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var showDatePicker by  remember { mutableStateOf(false)}
    var showAddExpenseModal by remember { mutableStateOf(false)}

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if(showDatePicker){
        DatePickerModal(
            onDateSelected = { date ->
                if (date != null) {
                    selectedDate = date
                }
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    val incrementDate: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        selectedDate = calendar.timeInMillis
    }

    val decrementDate: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        selectedDate = calendar.timeInMillis
    }

    val filteredExpenses = expenses.filter { isSameDay(it.date, selectedDate) }
    val totalForDay = getTotalAmount(filteredExpenses)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                user = user,
                onSignOutClick = {
                    scope.launch {
                        drawerState.close()
                    }
                    onSignOutClick()
                }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            containerColor = Color(0xFFF5F1E5),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddExpenseModal = true},
                    containerColor = Color(0xFFE0B663),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.add_desc),
                        tint = Color(0xFF2D2D2D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TopBar(
                    user = user,
                    onProfileClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                WelcomeSection(userName = user?.displayName ?: "User")

                Spacer(modifier = Modifier.height(16.dp))

                TimePeriodTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                DateNavigationRow(
                    selectedDate = selectedDate,
                    onPreviousDate = decrementDate,
                    onNextDate = incrementDate,
                    onShowDatePicker = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Total: ₹$totalForDay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExpenses) { expense ->
                        ExpenseCard(expense)
                    }
                }

                if (showAddExpenseModal) {
                    AddExpenseModal(
                        onDismiss = { showAddExpenseModal = false },
                        onSave = { name, category, date, amount ->
                            // TODO: Handle saving the expense to database
                            Log.d("AddExpense", "Name: $name, Category: $category, Date: $date, Amount: ₹$amount")
                            showAddExpenseModal = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(userName: String) {
    Text(
        text = "Welcome, $userName",
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF2D2D2D),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}
