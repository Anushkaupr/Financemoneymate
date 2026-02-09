package com.example.financemoneymate.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financemoneymate.model.UserAdminModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().getReference("Users") // Matches "Users" in Firebase

    var userList by remember { mutableStateOf(listOf<UserAdminModel>()) }
    var userToDelete by remember { mutableStateOf<UserAdminModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Real-time listener updates the total count and list automatically
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<UserAdminModel>()
                snapshot.children.forEach { child ->
                    // Manually mapping keys to fix "null null" error
                    val fName = child.child("firstName").value?.toString() ?: "No Name"
                    val lName = child.child("lastName").value?.toString() ?: ""
                    val emailAddr = child.child("email").value?.toString() ?: "No Email"
                    val bDay = child.child("dob").value?.toString() ?: ""

                    items.add(UserAdminModel(
                        userId = child.key ?: "", // The unique Firebase ID string
                        firstName = fName,
                        lastName = lName,
                        email = emailAddr,
                        dob = bDay
                    ))
                }
                userList = items
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Deletion Logic
    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Registered User?") },
            text = { Text("Are you sure you want to remove ${userToDelete?.firstName} from Firebase permanently?") },
            confirmButton = {
                TextButton(onClick = {
                    // This deletes the specific node in Firebase using its key
                    database.child(userToDelete!!.userId).removeValue().addOnSuccessListener {
                        Toast.makeText(context, "User removed from database", Toast.LENGTH_SHORT).show()
                    }
                    showDeleteDialog = false
                }) { Text("DELETE", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("CANCEL") }
            }
        )
    }

    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    auth.signOut()
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    (context as android.app.Activity).finish()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) { Text("LOGOUT ADMIN", fontWeight = FontWeight.Bold) }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Admin Control Panel", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Authorized: ${auth.currentUser?.email}", fontSize = 12.sp, color = Color.Gray)

            // Total Users Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TOTAL REGISTERED USERS", color = Color.White.copy(0.7f), fontSize = 10.sp)
                    // size updates automatically via the listener
                    Text("${userList.size}", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Text("Registered Users List", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userList) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // Now showing correct data
                                Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(user.email, color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                userToDelete = user
                                showDeleteDialog = true
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}