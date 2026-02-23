package com.example.financemoneymate.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag // REQUIRED FOR TESTING
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financemoneymate.R
import com.example.financemoneymate.repository.UserRepoImpl
import com.example.financemoneymate.viewmodel.UserViewModel

class ForgetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgetPasswordBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgetPasswordBody() {

    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    var email by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.bglogin_signup),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Forgot Password?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your email to receive a reset link",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // EMAIL INPUT FIELD
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("abc@gmail.com") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgotEmailField"), // ADDED TAG
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RESET BUTTON
            Button(
                onClick = {
                    if (email.isBlank()) {
                        Toast.makeText(
                            context,
                            "Please enter email",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        userViewModel.forgetPassword(email) { success, message ->
                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_LONG
                            ).show()

                            if (success) {
                                // Navigate to Login page
                                context.startActivity(
                                    Intent(context, LoginActivity::class.java)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("resetButton"), // ADDED TAG
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5)
                )
            ) {
                Text(
                    text = "Reset Password",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Remembered your password? Login",
                color = Color(0xFF1E88E5),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    context.startActivity(
                        Intent(context, LoginActivity::class.java)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgetPasswordPreview() {
    ForgetPasswordBody()
}