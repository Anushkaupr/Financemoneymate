package com.example.financemoneymate.view

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.financemoneymate.R
// IMPORTANT: Ensure these model imports match your actual package structure

import com.example.financemoneymate.utils.CloudinaryConfig
import com.example.financemoneymate.viewmodel.ExpenseViewModel
import com.example.financemoneymate.viewmodel.IncomeViewModel
import com.example.financemoneymate.viewmodel.SavingsViewModel
import com.example.financemoneymate.viewmodel.WishlistViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary if not already initialized
        try {
            val config = mapOf("cloud_name" to CloudinaryConfig.CLOUD_NAME, "secure" to true)
            MediaManager.init(this, config)
        } catch (e: Exception) {}

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
    incomeViewModel: IncomeViewModel = viewModel(),
    wishlistViewModel: WishlistViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var selectedIndex by remember { mutableStateOf(0) }

    val listNav = listOf(
        Pair("Home", R.drawable.baseline_home_24),
        Pair("Income", R.drawable.baseline_home_24),
        Pair("Expense", R.drawable.baseline_explicit_24),
        Pair("Savings", R.drawable.baseline_savings_24),
        Pair("More", R.drawable.baseline_more_horiz_24)
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
                        Box(modifier = Modifier.size(35.dp).background(Color(0xFF6200EE), CircleShape), contentAlignment = Alignment.Center) {
                            Text(currentUser?.email?.take(1)?.uppercase() ?: "U", color = Color.White, fontWeight = FontWeight.Bold)
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
                        label = { Text(item.first) },
                        icon = { Icon(painter = painterResource(item.second), contentDescription = null) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FD))) {
            when (selectedIndex) {
                0 -> HomeScreen(savingsViewModel, expenseViewModel, incomeViewModel, wishlistViewModel)
                1 -> IncomeBody(incomeViewModel)
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
    incomeVM: IncomeViewModel,
    wishlistVM: WishlistViewModel
) {
    val savingsList by savingsVM.savingsList.collectAsState()
    val expenseList by expenseVM.expenseList.collectAsState()
    val incomeList by incomeVM.incomeList.collectAsState()
    val wishlistItems by wishlistVM.wishlistItems.collectAsState()

    val totalIncome = incomeList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalSavings = savingsList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val totalExpenses = expenseList.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }.toInt()
    val balance = totalIncome + totalSavings - totalExpenses

    var wishName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL AVAILABLE BALANCE", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text("Rs. $balance", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                    MiniStatBox("TOTAL INCOME", "$totalIncome")
                    MiniStatBox("TOTAL SAVINGS", "+$totalSavings")
                    MiniStatBox("TOTAL EXPENSES", "-$totalExpenses")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(0.2f))

                Text("MY WISHLIST", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    TextField(
                        value = wishName,
                        onValueChange = { wishName = it },
                        placeholder = { Text("Wishing for?", fontSize = 11.sp, color = Color.LightGray) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedContainerColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(painterResource(R.drawable.baseline_camera_alt_24), contentDescription = null, tint = if(selectedImageUri != null) Color.Green else Color.White)
                    }
                    Button(
                        onClick = {
                            if (wishName.isNotEmpty() && selectedImageUri != null) {
                                isUploading = true
                                MediaManager.get().upload(selectedImageUri).unsigned(CloudinaryConfig.UPLOAD_PRESET)
                                    .callback(object : UploadCallback {
                                        override fun onSuccess(requestId: String?, resultData: Map<out Any?, Any?>?) {
                                            val url = resultData?.get("secure_url").toString()
                                            wishlistVM.addWish(wishName, url)
                                            wishName = ""; selectedImageUri = null; isUploading = false
                                        }
                                        override fun onError(requestId: String?, error: ErrorInfo?) {
                                            isUploading = false
                                            Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                                        }
                                        override fun onStart(requestId: String?) {}
                                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                    }).dispatch()
                            }
                        },
                        enabled = !isUploading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF6200EE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isUploading) "..." else "Add", fontWeight = FontWeight.Bold)
                    }
                }

                LazyRow(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(wishlistItems) { item ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(70.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(item.imageUrl),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(18.dp).clickable { wishlistVM.deleteWish(item.id) },
                                    color = Color.Red,
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Text(
                                text = item.wishName,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp).width(70.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    Icon(painter = painterResource(if (type == "Income") R.drawable.baseline_home_24 else R.drawable.baseline_explicit_24), contentDescription = null, modifier = Modifier.size(12.dp), tint = color)
                }
                Spacer(Modifier.width(6.dp))
                Text(title, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
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
                    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), color = Color(0xFF0D1117), shape = RoundedCornerShape(8.dp)) {
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
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Button(onClick = { auth.signOut(); activity.finish() }, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)), shape = RoundedCornerShape(12.dp)) {
            Text("LOGOUT", fontWeight = FontWeight.Bold)
        }
    }
}