package com.example.financemoneymate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.financemoneymate.view.SavingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SavingsViewModel : ViewModel() {

    // Get the current user's ID from Firebase Auth
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // Reference points to users -> [uniqueId] -> savings
    private val db = FirebaseDatabase.getInstance().getReference("users")
        .child(userId ?: "anonymous")
        .child("savings")

    private val _savingsList = MutableStateFlow<List<SavingItem>>(emptyList())
    val savingsList: StateFlow<List<SavingItem>> = _savingsList

    init {
        // Only fetch if user is logged in
        if (userId != null) {
            fetchSavings()
        }
    }

    private fun fetchSavings() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<SavingItem>()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val amount = child.child("amount").getValue(String::class.java) ?: ""
                    items.add(SavingItem(child.key ?: "", name, amount))
                }
                _savingsList.value = items
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addSavingToFirebase(name: String, amount: String) {
        if (userId == null) return
        val id = db.push().key ?: return
        val data = mapOf("name" to name, "amount" to amount)
        db.child(id).setValue(data)
    }

    fun updateSavingInFirebase(fId: String, name: String, amount: String) {
        if (userId != null && fId.isNotEmpty()) {
            val updates = mapOf("name" to name, "amount" to amount)
            db.child(fId).updateChildren(updates)
        }
    }

    fun deleteSavingFromFirebase(id: String) {
        if (userId != null) {
            db.child(id).removeValue()
        }
    }
}