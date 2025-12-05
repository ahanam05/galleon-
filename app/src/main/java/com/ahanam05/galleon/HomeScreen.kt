package com.ahanam05.galleon

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

data class ExpenseItem(
    val title: String,
    val category: String,
    val amount: String,
    val categoryTag: String? = null,
    val categoryColor: Color = Color(0xFFF7E8CA)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onSignOutClick: () -> Unit, user: FirebaseUser?) {
    val expenses = remember {
        listOf(
            ExpenseItem("Coffee", "Food", "$5.50", "Category"),
            ExpenseItem("Groceries", "Food",  "$45.80", "Shopping"),
            ExpenseItem("Bus Fare", "Transportation",  "$2.75", "Transport"),
            ExpenseItem("Dinner", "Food",  "$32.00", "Transport"),
            ExpenseItem("Snacks", "Food", "$4.20", "Category"),
            ExpenseItem("Movie Ticket", "Entertainment",  "$12.00", "Leisure"),
            ExpenseItem("Taxi", "Transportation",  "$18.60", "Transport")
        )
    }

    var selectedTab by remember { mutableStateOf("Daily") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                    onClick = { /* Add expense */ },
                    containerColor = Color(0xFFE0B663),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
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

                DateNavigationRow()

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseCard(expense)
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    user: FirebaseUser?,
    onSignOutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFFFFF5DD),
        modifier = Modifier.width(250.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = stringResource(id = R.string.profile_img_desc),
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0B663)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFE0B663), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: stringResource(id = R.string.profile_img_placeholder),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.displayName ?: "User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = Color(0xFFE0B663).copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(id = R.string.sign_out_text),
                        tint = Color(0xFF2D2D2D)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.sign_out_text),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D)
                    )
                },
                selected = false,
                onClick = onSignOutClick,
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                )
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.settings_desc),
                        tint = Color(0xFF2D2D2D)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = R.string.settings_desc),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D)
                    )
                },
                selected = false,
                onClick = { /* Navigate to settings */},
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(id = R.string.galleon_version_text),
                fontSize = 12.sp,
                color = Color(0xFF999999),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
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

@Composable
fun TopBar(user: FirebaseUser?, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 13.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { onProfileClick() }
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = stringResource(id = R.string.profile_img_desc),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFE0B663)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFE0B663), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user?.displayName?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(id = R.string.my_expenses_text),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D2D2D),
        )
    }
}

@Composable
fun TimePeriodTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf("Daily", "Weekly", "Monthly")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab

            Surface(
                color = if (selected) Color(0xFFE0B663) else Color.White,
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onTabSelected(tab) }
                ) {
                    Text(
                        text = tab,
                        color = if (selected) Color.Black else Color(0xFF777777),
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

}

@Composable
fun DateNavigationRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { /* Navigate to previous date */ }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(id = R.string.previous_date_desc),
                tint = Color(0xFF666666)
            )
        }

        Text(
            text = "Wednesday, July 31",
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2D2D2D)
        )

        IconButton(onClick = { /* Navigate to next date */ }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(id = R.string.next_date_desc),
                tint = Color(0xFF666666)
            )
        }
        IconButton(onClick = { /* Open calendar */ }) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(id = R.string.calendar_icon_desc),
                tint = Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ExpenseCard(expense: ExpenseItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F7EA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(48.dp)
                    .background(Color(0xFFE0B663), RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
                Text(
                    text = expense.category,
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                expense.categoryTag?.let { tag ->
                    Surface(
                        color = expense.categoryColor,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 10.sp,
                            color = Color(0xFF6B6B6B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = expense.amount,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2D2D)
                )
            }
        }
    }
}
