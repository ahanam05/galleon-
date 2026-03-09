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
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahanam05.galleon.home.components.ExpenseModal
import com.ahanam05.galleon.home.components.DateNavigationRow
import com.ahanam05.galleon.home.components.DatePickerModal
import com.ahanam05.galleon.home.components.ExpenseCard
import com.ahanam05.galleon.home.components.NavigationDrawerContent
import com.ahanam05.galleon.home.components.TimePeriodTabs
import com.ahanam05.galleon.home.components.TopBar
import com.ahanam05.galleon.home.components.BudgetDialog
import com.ahanam05.galleon.home.components.BudgetStatusCard
import com.ahanam05.galleon.isSameDay
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import com.ahanam05.galleon.R
import com.ahanam05.galleon.formatDate
import com.ahanam05.galleon.formatMonth
import com.ahanam05.galleon.formatWeekRange
import com.ahanam05.galleon.getTotalAmount
import com.ahanam05.galleon.getMonthYear
import com.ahanam05.galleon.data.models.Budget
import com.ahanam05.galleon.home.components.DailyBreakdownChart
import com.ahanam05.galleon.home.components.MonthlyExpenseCard
import com.ahanam05.galleon.home.components.NoExpensesFound
import com.ahanam05.galleon.data.aggregator.ExpenseAggregator

object Modes{
    const val DAILY = "Daily"
    const val WEEKLY = "Weekly"
    const val MONTHLY = "Monthly"
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSignOutClick: () -> Unit,
               user: FirebaseUser?,
               viewModel: HomeViewModel = hiltViewModel()) {
    val expenses by viewModel.expenses.collectAsState()

    val weeklyTotal by viewModel.weeklyTotal.collectAsState()
    val dailyBreakdownByDay by viewModel.dailyBreakdownByDay.collectAsState()
    val dailyAverageAmount by viewModel.dailyAverageAmount.collectAsState()
    val weekStartDate by viewModel.weekStartDate.collectAsState()
    val weekEndDate by viewModel.weekEndDate.collectAsState()
    val topCategory by viewModel.topCategory.collectAsState()
    val monthlyTotal by viewModel.monthlyTotal.collectAsState()
    val monthlyComparison by viewModel.monthlyComparison.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()

    val MutedGold = Color(0xFFDDAA44)
    var selectedTab by remember { mutableStateOf(Modes.DAILY) }
    var selectedDate by remember { mutableLongStateOf(Calendar.getInstance().timeInMillis) }
    var showDatePicker by  remember { mutableStateOf(false)}
    var showExpenseModal by remember { mutableStateOf(false)}
    var showBudgetDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedDate) {
        viewModel.updateSelectedDate(selectedDate)
    }

    LaunchedEffect(selectedTab) {
        viewModel.updateSelectedTab(selectedTab)
    }

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

    if (showBudgetDialog) {
        BudgetDialog(
            formattedMonth = formatMonth(selectedDate),
            existingBudget = monthlyBudget,
            monthYear = getMonthYear(selectedDate),
            onDismiss = { showBudgetDialog = false },
            onSave = { budgetAmount ->
                val monthYearKey = getMonthYear(selectedDate)
                val budget = Budget(monthYear = monthYearKey, monthlyBudget = budgetAmount.monthlyBudget)
                viewModel.setMonthlyBudget(monthYearKey, budget)
                showBudgetDialog = false
                viewModel.updateSelectedDate(selectedDate)
            }
        )
    }

    val incrementDate: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        when (selectedTab) {
            Modes.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            Modes.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            Modes.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        }
        selectedDate = calendar.timeInMillis
    }

    val decrementDate: () -> Unit = {
        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
        when (selectedTab) {
            Modes.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, -1)
            Modes.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            Modes.MONTHLY -> calendar.add(Calendar.MONTH, -1)
        }
        selectedDate = calendar.timeInMillis
    }

    val filteredExpenses = expenses.filter { isSameDay(it.date, selectedDate) }
    val totalForDay = getTotalAmount(filteredExpenses)
    val weeklyFilteredExpenses = expenses.filter { expense ->
        expense.date in weekStartDate..weekEndDate
    }

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
            containerColor = Color(0xFFF9F7F0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showExpenseModal = true},
                    containerColor = MutedGold,
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

                TimePeriodTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                DateNavigationRow(
                    selectedDate = when (selectedTab) {
                        Modes.WEEKLY -> formatWeekRange(weekStartDate, weekEndDate)
                        Modes.MONTHLY -> formatMonth(selectedDate)
                        else -> formatDate(selectedDate)
                    },
                    onPreviousDate = decrementDate,
                    onNextDate = incrementDate,
                    onShowDatePicker = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                BudgetStatusCard(
                    monthlyTotal = monthlyTotal,
                    budget = monthlyBudget,
                    percentageSpent = ExpenseAggregator.getBudgetPercentageSpent(
                        monthlyTotal,
                        monthlyBudget?.monthlyBudget ?: 0.0),
                    remainingAmount = ExpenseAggregator.getRemainingBudget(
                        monthlyTotal,
                        monthlyBudget?.monthlyBudget ?: 0.0
                    ),
                    formattedMonth = formatMonth(selectedDate),
                    onClick = { showBudgetDialog = true },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Conditional rendering based on selected tab
                when (selectedTab) {
                    Modes.DAILY -> {
                        Text(
                            text = "Total: ₹$totalForDay",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (filteredExpenses.isEmpty()) {
                            NoExpensesFound()
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredExpenses) { expense ->
                                    ExpenseCard(
                                        expense = expense,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }

                    Modes.WEEKLY -> {
                        DailyBreakdownChart(
                            dailyBreakdown = dailyBreakdownByDay,
                            weekStartDate = weekStartDate,
                            weeklyTotal = weeklyTotal,
                            topCategory = topCategory,
                            dailyAverage = dailyAverageAmount
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Modes.MONTHLY -> {
                        MonthlyExpenseCard(
                            monthlyTotal = monthlyTotal,
                            comparisonText = monthlyComparison.first,
                            isIncrease = monthlyComparison.second,
                            shouldShowComparison = monthlyComparison.third
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
        }

                if (showExpenseModal) {
                    ExpenseModal(
                        onDismiss = { showExpenseModal = false },
                        onSave = { expense, name, category, date, amount ->
                            Log.d("Add Expense", "Name: $name, Category: $category, Date: $date, Amount: ₹$amount")
                            viewModel.addExpense(name, category, amount, date)
                            showExpenseModal = false
                        },
                        onDelete = {},
                        title = stringResource(id = R.string.add_expense_text),
                        existingExpense = null,
                        selectedDate = selectedDate
                    )
                }
            }
        }
    }
}
