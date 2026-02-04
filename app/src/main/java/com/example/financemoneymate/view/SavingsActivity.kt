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
import com.example.financemoneymate.view.ui.theme.FinancemoneymateTheme
import com.example.financemoneymate.viewmodel.SavingsViewModel

val DarkBlueBg = Color(0xFF1A2232)
val CardInnerBg = Color(0xFF242F41)
val SuccessGreen = Color(0xFF00C853)
val ActionYellow = Color(0xFFFFC107)
val ActionRed = Color(0xFFF44336)

data class SavingItem(val fId: String = "", val name: String = "", val amount: String = "")

class SavingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinancemoneymateTheme {
                SavingsBody()
            }
        }
    }
}

@Composable
fun SavingsBody(viewModel: SavingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    var savingFieldName by remember { mutableStateOf("") }
    var savingAmount by remember { mutableStateOf("") }

    // NEW: Track the ID of the item being edited. Empty means "Create Mode".
    var editingfId by remember { mutableStateOf("") }

    val savingsList by viewModel.savingsList.collectAsState()

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
                Text("Savings", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("John Doe", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("john@example.com", color = Color.Gray, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF4A3AFF), CircleShape), contentAlignment = Alignment.Center) {
                        Text("JD", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Savings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = savingFieldName,
                            onValueChange = { savingFieldName = it },
                            placeholder = { Text("Saving Field", color = Color.Gray) },
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
                            value = savingAmount,
                            onValueChange = { savingAmount = it },
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

                        // Action Button (Dynamic: Create or Update)
                        Button(
                            onClick = {
                                if (savingFieldName.isNotBlank() && savingAmount.isNotBlank()) {
                                    if (editingfId.isEmpty()) {
                                        viewModel.addSavingToFirebase(savingFieldName, savingAmount)
                                    } else {
                                        viewModel.updateSavingInFirebase(editingfId, savingFieldName, savingAmount)
                                        editingfId = "" // Clear edit mode
                                    }
                                    savingFieldName = ""
                                    savingAmount = ""
                                } else {
                                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editingfId.isEmpty()) SuccessGreen else ActionYellow
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(if (editingfId.isEmpty()) "Create" else "Update", fontSize = 12.sp, color = if (editingfId.isEmpty()) Color.White else Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Table Header
                    Row(modifier = Modifier.fillMaxWidth().background(CardInnerBg).padding(8.dp)) {
                        Text("Saving Field", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Amount", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Actions", Modifier.weight(1f), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(savingsList) { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(item.name, Modifier.weight(1f), color = Color.White)
                                Text(item.amount, Modifier.weight(1f), color = Color.White)
                                Row(Modifier.weight(1f)) {
                                    // EDIT BUTTON
                                    Button(
                                        onClick = {
                                            savingFieldName = item.name
                                            savingAmount = item.amount
                                            editingfId = item.fId // Set the ID to trigger update mode
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ActionYellow),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Edit", color = Color.Black, fontSize = 10.sp)
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    // DELETE BUTTON
                                    Button(
                                        onClick = {
                                            if (item.fId.isNotEmpty()) {
                                                viewModel.deleteSavingFromFirebase(item.fId)
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
//jkih