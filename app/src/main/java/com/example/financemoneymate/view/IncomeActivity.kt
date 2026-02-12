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
import com.example.financemoneymate.view.ui.theme.* // Ensure your theme colors are imported
import com.example.financemoneymate.viewmodel.IncomeViewModel

// Data Class for the List
data class IncomeItem(val fId: String = "", val name: String = "", val amount: String = "")

class IncomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancemoneymateTheme {
                IncomeBody()
            }
        }
    }
}

@Composable
fun IncomeBody(viewModel: IncomeViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser

    var incomeName by remember { mutableStateOf("") }
    var incomeAmount by remember { mutableStateOf("") }
    var editingFId by remember { mutableStateOf("") }

    val incomeList by viewModel.incomeList.collectAsState()

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
                Text("Income", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3AFF))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = currentUser?.displayName ?: "User", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = currentUser?.email ?: "No Email", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(40.dp).background(Color(0xFF4A3AFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentUser?.displayName?.take(1)?.uppercase() ?: "U", color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Input Card (Green themed for Income)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2738)), // Using your dark blue style
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Add Income Source", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = incomeName,
                            onValueChange = { incomeName = it },
                            placeholder = { Text("Source (e.g. Salary)", color = Color.Gray) },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0D1117), unfocusedContainerColor = Color(0xFF0D1117)
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = incomeAmount,
                            onValueChange = { incomeAmount = it },
                            placeholder = { Text("Amt", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0D1117), unfocusedContainerColor = Color(0xFF0D1117)
                            )
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (incomeName.isNotBlank() && incomeAmount.isNotBlank()) {
                                if (editingFId.isEmpty()) {
                                    viewModel.addIncomeToFirebase(incomeName, incomeAmount)
                                } else {
                                    viewModel.updateIncomeInFirebase(editingFId, incomeName, incomeAmount)
                                    editingFId = ""
                                }
                                incomeName = ""; incomeAmount = ""
                            } else {
                                Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (editingFId.isEmpty()) Color(0xFF2ECC71) else Color(0xFFF1C40F)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (editingFId.isEmpty()) "Add Income" else "Update Income", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // List
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        items(incomeList) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text("Rs. ${item.amount}", color = Color(0xFF2ECC71), fontSize = 12.sp)
                                }
                                Row {
                                    IconButton(onClick = {
                                        incomeName = item.name
                                        incomeAmount = item.amount
                                        editingFId = item.fId
                                    }) {
                                        Text("Edit", color = Color(0xFFF1C40F), fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { viewModel.deleteIncomeFromFirebase(item.fId) }) {
                                        Text("Del", color = Color(0xFFE74C3C), fontSize = 12.sp)
                                    }
                                }
                            }
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                        }
                    }
                }
            }
        }
    }
}