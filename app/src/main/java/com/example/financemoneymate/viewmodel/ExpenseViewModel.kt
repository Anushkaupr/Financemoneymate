package com.example.financemoneymate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.financemoneymate.view.ExpenseItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExpenseViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // Reference points to users -> [userId] -> expenses
    private val db = FirebaseDatabase.getInstance().getReference("users")
        .child(userId ?: "anonymous")
        .child("expenses")

    private val _expenseList = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenseList: StateFlow<List<ExpenseItem>> = _expenseList

    init {
        if (userId != null) {
            fetchExpenses()
        }
    }

    private fun fetchExpenses() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ExpenseItem>()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val amount = child.child("amount").getValue(String::class.java) ?: ""
                    items.add(ExpenseItem(child.key ?: "", name, amount))
                }
                _expenseList.value = items
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addExpenseToFirebase(name: String, amount: String) {
        if (userId == null) return
        val id = db.push().key ?: return
        val data = mapOf("name" to name, "amount" to amount)
        db.child(id).setValue(data)
    }

    fun updateExpenseInFirebase(fId: String, name: String, amount: String) {
        if (fId.isNotEmpty()) {
            val updates = mapOf("name" to name, "amount" to amount)
            db.child(fId).updateChildren(updates)
        }
    }

    fun deleteExpenseFromFirebase(id: String) {
        if (id.isNotEmpty()) {
            db.child(id).removeValue()
        }
    }
}