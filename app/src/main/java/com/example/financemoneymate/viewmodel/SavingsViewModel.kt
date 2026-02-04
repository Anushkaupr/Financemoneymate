package com.example.financemoneymate.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.financemoneymate.view.SavingItem

class SavingsViewModel : ViewModel() {
    private val db = FirebaseDatabase.getInstance().getReference("savings")

    private val _savingsList = MutableStateFlow<List<SavingItem>>(emptyList())
    val savingsList: StateFlow<List<SavingItem>> = _savingsList

    init {
        fetchSavings()
    }

    private fun fetchSavings() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<SavingItem>()
                snapshot.children.forEach { child ->
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val amount = child.child("amount").getValue(String::class.java) ?: ""
                    // Pass child.key directly as the first argument (fId)
                    items.add(SavingItem(child.key ?: "", name, amount))
                }
                _savingsList.value = items
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addSavingToFirebase(name: String, amount: String) {
        val id = db.push().key ?: return
        val data = mapOf("name" to name, "amount" to amount)
        db.child(id).setValue(data)
    }
    fun updateSavingInFirebase(fId: String, name: String, amount: String) {
        if (fId.isNotEmpty()) {
            val updates = mapOf(
                "name" to name,
                "amount" to amount
            )
            db.child(fId).updateChildren(updates)
        }
    }
    fun deleteSavingFromFirebase(id: String) {
        // Note: Real implementation would need the actual Firebase Key
        // For simplicity here, we assume id passed is the Firebase key
        db.child(id).removeValue()
    }
}