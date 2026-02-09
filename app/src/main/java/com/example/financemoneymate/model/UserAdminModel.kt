package com.example.financemoneymate.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserAdminModel(
    val userId: String = "",
    val firstName: String = "", // Matches Firebase key exactly
    val lastName: String = "",  // Matches Firebase key exactly
    val email: String = "",     // Matches Firebase key exactly
    val dob: String = ""
)