package com.example.financemoneymate.view

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemoneymate.R
import com.example.financemoneymate.viewmodel.ExpenseViewModel
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
    expenseViewModel: ExpenseViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val sharedPref = remember { context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE) }

    var selectedIndex by remember { mutableStateOf(0) }

    data class NavItem(val label: String, val icon: Int)
    val listNav = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
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
                0 -> HomeScreen(savingsViewModel, expenseViewModel, sharedPref)
                1 -> ExpenseBody(expenseViewModel)
                2 -> SavingsBody(savingsViewModel)
                3 -> MoreScreen(auth, activity)
            }
        }
    }
}

@Composable
fun HomeScreen(
    savingsVM: SavingsViewModel,
    expenseVM: ExpenseViewModel,
    sharedPref: android.content.SharedPreferences
) {
    val savingsList by savingsVM.savingsList.collectAsState()
    val expenseList by expenseVM.expenseList.collectAsState()

    var incomeInput by remember { mutableStateOf(sharedPref.getString("base_income", "0") ?: "0") }

    val totalSavings = savingsList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalExpenses = expenseList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val baseIncome = incomeInput.toIntOrNull() ?: 0
    val balance = baseIncome + totalSavings - totalExpenses

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // 1. Dynamic Income Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2232)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("STARTING MONTHLY INCOME", color = Color(0xFF6C63FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Update to adjust balance", color = Color.LightGray, fontSize = 10.sp)
                }
                OutlinedTextField(
                    value = incomeInput,
                    onValueChange = {
                        incomeInput = it
                        sharedPref.edit().putString("base_income", it).apply()
                    },
                    prefix = { Text("Rs. ", color = Color.White) },
                    modifier = Modifier.width(110.dp).height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0D1117),
                        unfocusedContainerColor = Color(0xFF0D1117),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Purple Balance Card
        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text("Rs. $balance", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStatBox("INCOME", "$baseIncome")
                    MiniStatBox("SAVINGS", "+$totalSavings")
                    MiniStatBox("EXPENSE", "-$totalExpenses")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. The "Pile Up" Transaction List
        Text("Recent Transactions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Newest Savings first
            items(savingsList.asReversed()) { item ->
                TransactionItem(item.name, "+ Rs. ${item.amount}", Color(0xFF4CAF50), "Saving")
            }
            // Newest Expenses first
            items(expenseList.asReversed()) { item ->
                TransactionItem(item.name, "- Rs. ${item.amount}", Color(0xFFF44336), "Expense")
            }
        }
    }
}

@Composable
fun TransactionItem(name: String, amount: String, color: Color, type: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2232)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(type, color = Color.Gray, fontSize = 11.sp)
            }
            Text(amount, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                activity.finish() // Closes dashboard and returns to Login
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painter = painterResource(R.drawable.baseline_more_horiz_24), contentDescription = null) // Replace with logout icon if you have one
            Spacer(Modifier.width(8.dp))
            Text("LOGOUT FROM ACCOUNT", fontWeight = FontWeight.Bold)
        }
    }
}