package com.ashuh.thoughtbank.api

import com.ashuh.thoughtbank.BuildKonfig.FIREBASE_API_KEY
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object FirebaseAuthApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun signUp(email: String, password: String): AuthResponse {
        val responseBody = client
            .post("https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$FIREBASE_API_KEY") {
                header("Content-Type", "application/json")
                parameter("email", email)
                parameter("password", password)
                parameter("returnSecureToken", true)
            }
        if (responseBody.status.value == 200) {
            return json.decodeFromString<AuthResponse>(responseBody.bodyAsText())
        } else {
            val response = json.decodeFromString<ErrorResponse>(responseBody.bodyAsText())
            throw Exception(response.error.message)
        }
    }

    suspend fun signIn(email: String, password: String): AuthResponse {
        val responseBody = client
            .post("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY") {
                header("Content-Type", "application/json")
                parameter("email", email)
                parameter("password", password)
                parameter("returnSecureToken", true)
            }
        if (responseBody.status.value == 200) {
            return json.decodeFromString<AuthResponse>(responseBody.bodyAsText())
        } else {
            val response = json.decodeFromString<ErrorResponse>(responseBody.bodyAsText())
            throw Exception(response.error.message)
        }
    }
}
