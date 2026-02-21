package com.example.financemoneymate.view

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financemoneymate.view.ui.theme.FinancemoneymateTheme
import com.example.financemoneymate.viewmodel.ExpenseViewModel

// Theme Colors


// Data Class for the List
data class ExpenseItem(val fId: String = "", val name: String = "", val amount: String = "")

class ExpenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancemoneymateTheme {
                ExpenseBody()
            }
        }
    }
}

@Composable
fun ExpenseBody(viewModel: ExpenseViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    // State variables for inputs
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var editingFId by remember { mutableStateOf("") }

    // Collect list from ViewModel
    val expenseList by viewModel.expenseList.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Expenses", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Manage Expenses", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            placeholder = { Text("Expensed Reason", color = Color.Gray) },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E2738),
                                unfocusedContainerColor = Color(0xFF1E2738),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            placeholder = { Text("Amount", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1E2738),
                                unfocusedContainerColor = Color(0xFF1E2738),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.width(8.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (expenseName.isNotBlank() && expenseAmount.isNotBlank()) {
                                    if (editingFId.isEmpty()) {
                                        viewModel.addExpenseToFirebase(expenseName, expenseAmount)
                                    } else {
                                        viewModel.updateExpenseInFirebase(editingFId, expenseName, expenseAmount)
                                        editingFId = ""
                                    }
                                    expenseName = ""
                                    expenseAmount = ""
                                } else {
                                    Toast.makeText(context, "All fields required", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editingFId.isEmpty()) SuccessGreen else ActionYellow
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = if (editingFId.isEmpty()) "Add" else "Fix",
                                fontSize = 12.sp,
                                color = if (editingFId.isEmpty()) Color.White else Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Table Header
                    Row(modifier = Modifier.fillMaxWidth().background(CardInnerBg).padding(8.dp)) {
                        Text("Item", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Amount", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Actions", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Expenses List
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(expenseList) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.name, Modifier.weight(1f), color = Color.White)
                                Text("Rs.${item.amount}", Modifier.weight(1f), color = Color.White)
                                Row(Modifier.weight(1f)) {
                                    // Edit Button
                                    Button(
                                        onClick = {
                                            expenseName = item.name
                                            expenseAmount = item.amount
                                            editingFId = item.fId
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ActionYellow),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Edit", color = Color.Black, fontSize = 10.sp)
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    // Delete Button
                                    Button(
                                        onClick = {
                                            if (item.fId.isNotEmpty()) {
                                                viewModel.deleteExpenseFromFirebase(item.fId)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ActionRed),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Delete", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        }
                    }
                }
            }
        }
    }
}