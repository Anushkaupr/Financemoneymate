package com.example.financemoneymate.model

data class UserModel(
    val userId : String = "",
    val email : String = "",
    val firstName : String = "",
    val lastName : String = "",

){
    fun toMap() : Map<String,Any?>{
        return mapOf(
            "email" to email,
            "userId" to userId,
            "firstname" to firstName,
            "lastname" to lastName
        )
    }
}