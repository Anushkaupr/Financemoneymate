package com.example.financemoneymate.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemoneymate.R
import com.example.financemoneymate.viewmodel.ExpenseViewModel
import com.example.financemoneymate.viewmodel.IncomeViewModel
import com.example.financemoneymate.viewmodel.SavingsViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(
    savingsViewModel: SavingsViewModel = viewModel(),
    expenseViewModel: ExpenseViewModel = viewModel(),
    incomeViewModel: IncomeViewModel = viewModel() // Added Income ViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var selectedIndex by remember { mutableStateOf(0) }

    data class NavItem(val label: String, val icon: Int)
    val listNav = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Income", R.drawable.baseline_home_24), // Add Income Tab
        NavItem("Expense", R.drawable.baseline_explicit_24),
        NavItem("Savings", R.drawable.baseline_savings_24),
        NavItem("More", R.drawable.baseline_more_horiz_24)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FD)),
                title = { Text("DASHBOARD", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(currentUser?.displayName ?: "User", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(currentUser?.email ?: "No Email", color = Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(35.dp).background(Color(0xFF6200EE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = currentUser?.email?.take(1)?.uppercase() ?: "U"
                            Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listNav.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        label = { Text(item.label) },
                        icon = { Icon(painter = painterResource(item.icon), contentDescription = null) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FD))) {
            when (selectedIndex) {
                0 -> HomeScreen(savingsViewModel, expenseViewModel, incomeViewModel)
                1 -> IncomeBody(incomeViewModel) // Income Page link
                2 -> ExpenseBody(expenseViewModel)
                3 -> SavingsBody(savingsViewModel)
                4 -> MoreScreen(auth, activity)
            }
        }
    }
}

@Composable
fun HomeScreen(
    savingsVM: SavingsViewModel,
    expenseVM: ExpenseViewModel,
    incomeVM: IncomeViewModel
) {
    // These observers trigger a recomposition whenever Firebase data changes
    val savingsList by savingsVM.savingsList.collectAsState()
    val expenseList by expenseVM.expenseList.collectAsState()
    val incomeList by incomeVM.incomeList.collectAsState()

    // LIVE CALCULATIONS
    // .sumOf iterates through the current list state fetched from Firebase
    val totalIncome = incomeList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalSavings = savingsList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalExpenses = expenseList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()

    // Standard accounting logic for your dashboard:
    // Available Balance = Money Earned - Money Set Aside - Money Spent
    val balance = totalIncome + totalSavings - totalExpenses

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // Purple Balance Card (Visual: image_4df358.jpg)
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)

                // This Text will update automatically when 'balance' changes
                Text("Rs. $balance", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)

                Spacer(modifier = Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStatBox("TOTAL INCOME", "$totalIncome")
                    MiniStatBox("TOTAL SAVINGS", "+$totalSavings")
                    MiniStatBox("TOTAL EXPENSES", "-$totalExpenses")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Three Column Recent View (Matching image style)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RecentBox("Recent Income", incomeList, Modifier.weight(1f), Color(0xFF2196F3), "Income")
            RecentBox("Recent Savings", savingsList, Modifier.weight(1f), Color(0xFF4CAF50), "Saving")
            RecentBox("Recent Expenses", expenseList, Modifier.weight(1f), Color(0xFFF44336), "Expense")
        }
    }
}
@Composable
fun RecentBox(title: String, list: List<Any>, modifier: Modifier, color: Color, type: String) {
    Card(
        modifier = modifier.height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2232)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(18.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(if (type == "Income") R.drawable.baseline_home_24 else R.drawable.baseline_explicit_24),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = color
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                // Taking last 2 items
                items(list.takeLast(2).reversed()) { item ->
                    val name = when(item) {
                        is IncomeItem -> item.name
                        is ExpenseItem -> item.name
                        is SavingItem -> item.name
                        else -> ""
                    }
                    val amt = when(item) {
                        is IncomeItem -> "Rs. ${item.amount}"
                        is ExpenseItem -> "- Rs. ${item.amount}"
                        is SavingItem -> "+ Rs. ${item.amount}"
                        else -> ""
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        color = Color(0xFF0D1117),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, color = Color.White, fontSize = 9.sp, maxLines = 1)
                            Text(amt, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniStatBox(label: String, value: String) {
    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
            Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MoreScreen(auth: FirebaseAuth, activity: Activity) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings & Account", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = {
                auth.signOut()
                activity.finish()
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("LOGOUT FROM ACCOUNT", fontWeight = FontWeight.Bold)
        }
    }
}