package com.ashuh.thoughtbank.api

import Thought
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ThoughtBankApi {

    private const val BASE_URL = "https://thought-bank.fly.dev/"

    private val client = HttpClient() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun registerUsername(userId: String, username: String) {
        val response = client.post(BASE_URL + "register-username") {
            url {
                parameters.append("userId", userId)
                parameters.append("username", username)
            }
        }
        if (response.status.value != 200) {
            throw Exception("Username is already taken")
        }
    }

    suspend fun isUserRegistered(userId: String): Boolean {
        return client.get(BASE_URL + "is-registered") {
            url {
                parameters.append("userId", userId)
            }
        }.body()
    }

    suspend fun getThoughts(userId: String, numThoughts: Int): List<Thought> {
        return client.get(BASE_URL + "thoughts") {
            url {
                parameters.append("userId", userId)
                parameters.append("numThoughts", numThoughts.toString())
            }
        }.body()
    }

    suspend fun upvote(userId: String, thoughtId: Int) {
        client.post(BASE_URL + "upvote") {
            url {
                parameters.append("userId", userId)
                parameters.append("thoughtId", thoughtId.toString())
            }
        }
    }

    suspend fun downvote(userId: String, thoughtId: Int) {
        client.post(BASE_URL + "downvote") {
            url {
                parameters.append("userId", userId)
                parameters.append("thoughtId", thoughtId.toString())
            }
        }
    }

    suspend fun depositThought(userId: String, thoughtContent: String) {
        client.post(BASE_URL + "deposit-thought") {
            url {
                parameters.append("userId", userId)
                parameters.append("thoughtContent", thoughtContent)
            }
        }
    }

    suspend fun getUserUpvotedThoughts(userId: String): List<Thought> {
        return client.get(BASE_URL + "/user-upvoted-thoughts") {
            url {
                parameters.append("userId", userId)
            }
        }.body()
    }
}
