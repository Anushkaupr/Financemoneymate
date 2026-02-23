package com.example.financemoneymate

import com.example.financemoneymate.repository.UserRepo
import com.example.financemoneymate.viewmodel.UserViewModel
import com.example.financemoneymate.model.UserModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify


class UserViewModelTest {

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login success")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login success", messageResult)

        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun signup_and_database_save_success_test() {
        // 1. Setup
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        val testEmail = "test@gmail.com"
        val testPass = "123456"
        val testUserId = "user123"
        val testUser = UserModel(
            userId = testUserId,
            email = testEmail,
            firstName = "John",
            lastName = "Doe",
            dob = "01/01/2000"
        )

        // 2. Mock Step 1: Firebase Auth Signup
        // In your code: signup(email, password) { success, message, userId -> ... }
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Auth Success", testUserId)
            null
        }.`when`(repo).signup(eq(testEmail), eq(testPass), any())

        // 3. Mock Step 2: Database Save
        // In your code: addUserToDatabase(userId, user) { dbSuccess, dbMsg -> ... }
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Database Success")
            null
        }.`when`(repo).addUserToDatabase(eq(testUserId), any(), any())

        // 4. Execution
        var finalSuccess = false
        var finalMessage = ""

        viewModel.signup(testEmail, testPass) { success, msg, userId ->
            if (success) {
                viewModel.addUserToDatabase(userId, testUser) { dbSuccess, dbMsg ->
                    finalSuccess = dbSuccess
                    finalMessage = dbMsg
                }
            }
        }

        // 5. Assertions
        assertTrue(finalSuccess)
        assertEquals("Database Success", finalMessage)

        // 6. Verification
        verify(repo).signup(eq(testEmail), eq(testPass), any())
        verify(repo).addUserToDatabase(eq(testUserId), eq(testUser), any())
    }


}