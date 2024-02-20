package com.ashuh.thoughtbank.api

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse (
    val idToken : String,
    val email : String ,
    val refreshToken: String,
    val expiresIn: Int,
    val localId: String
)

@Serializable
data class ErrorResponse (
    val error : Error
)

@Serializable
data class Error (
    val code : Int,
    val message : String
)
