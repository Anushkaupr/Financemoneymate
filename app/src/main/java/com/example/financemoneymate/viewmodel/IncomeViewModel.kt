package com.example.financemoneymate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.financemoneymate.view.IncomeItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IncomeViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // Reference points to users -> [userId] -> income
    private val db = FirebaseDatabase.getInstance().getReference("users")
        .child(userId ?: "anonymous")
        .child("income")

    private val _incomeList = MutableStateFlow<List<IncomeItem>>(emptyList())
    val incomeList: StateFlow<List<IncomeItem>> = _incomeList

    init {
        if (userId != null) {
            fetchIncomes()
        }
    }

    private fun fetchIncomes() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<IncomeItem>()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val amount = child.child("amount").getValue(String::class.java) ?: ""
                    items.add(IncomeItem(child.key ?: "", name, amount))
                }
                _incomeList.value = items
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addIncomeToFirebase(name: String, amount: String) {
        if (userId == null) return
        val id = db.push().key ?: return
        val data = mapOf("name" to name, "amount" to amount)
        db.child(id).setValue(data)
    }

    fun updateIncomeInFirebase(fId: String, name: String, amount: String) {
        if (fId.isNotEmpty()) {
            val updates = mapOf("name" to name, "amount" to amount)
            db.child(fId).updateChildren(updates)
        }
    }

    fun deleteIncomeFromFirebase(id: String) {
        if (id.isNotEmpty()) {
            db.child(id).removeValue()
        }
    }
}