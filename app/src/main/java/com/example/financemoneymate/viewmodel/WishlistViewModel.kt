package com.example.financemoneymate.viewmodel

import androidx.lifecycle.ViewModel
import com.example.financemoneymate.model.WishlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WishlistViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _wishlistItems = MutableStateFlow<List<WishlistItem>>(emptyList())
    val wishlistItems: StateFlow<List<WishlistItem>> = _wishlistItems

    init {
        fetchWishlist()
    }

    private fun fetchWishlist() {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child("wishlist").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<WishlistItem>()
                for (child in snapshot.children) {
                    child.getValue(WishlistItem::class.java)?.let { items.add(it) }
                }
                _wishlistItems.value = items
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addWish(name: String, imageUrl: String) {
        val userId = auth.currentUser?.uid ?: return
        val wishId = database.child(userId).child("wishlist").push().key ?: return

        val newWish = WishlistItem(id = wishId, wishName = name, imageUrl = imageUrl)
        database.child(userId).child("wishlist").child(wishId).setValue(newWish)
    }
    fun deleteWish(wishId: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child(userId).child("wishlist").child(wishId).removeValue()
    }
}