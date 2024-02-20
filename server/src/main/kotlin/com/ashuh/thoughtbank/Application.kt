package com.ashuh.thoughtbank

import SERVER_PORT
import com.ashuh.thoughtbank.database.Database
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    install(CORS) {
        anyHost()
    }

    Database.init()

    routing {
        post("/register-username") {
            val userId = call.request.queryParameters["userId"]
            val username = call.request.queryParameters["username"]
            if (userId == null || username == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID or username")
                return@post
            }
            val isAvailable = Database.isUsernameAvailable(username)

            if (!isAvailable) {
                call.respond(HttpStatusCode.Conflict, "Username is already taken")
                return@post
            }
            Database.addUser(userId, username)
            call.respond(HttpStatusCode.OK)
        }

        get("/is-registered") {
            val userId = call.request.queryParameters["userId"]
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }
            val isRegistered = Database.isUserRegistered(userId)
            call.respond(isRegistered)
        }

        get("/thoughts") {
            val user = call.request.queryParameters["userId"]
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }

            val numThoughts = call.request.queryParameters["numThoughts"]?.toIntOrNull() ?: 10
            val newThoughts = Database.getUserUnseenThoughts(user, numThoughts)
            call.respond(newThoughts)
        }

        get("/user-upvoted-thoughts") {
            val user = call.request.queryParameters["userId"]
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@get
            }
            val upvotedThoughts = Database.getUserUpvotedThoughts(user)
            call.respond(upvotedThoughts)
        }

        post("/deposit-thought") {
            val userId = call.request.queryParameters["userId"]
            val thoughtContent = call.request.queryParameters["thoughtContent"]
            if (userId == null || thoughtContent == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID or thought content")
                return@post
            }
            Database.addThought(userId, thoughtContent)
            call.respond(HttpStatusCode.OK)
        }

        post("/upvote") {
            val userId = call.parameters["userId"]
            val thoughtId = call.parameters["thoughtId"]?.toIntOrNull()
            if (userId == null || thoughtId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID or thought ID")
                return@post
            }
            Database.upvoteThought(thoughtId, userId)
            call.respond(HttpStatusCode.OK)
        }

        post("/downvote") {
            val userId = call.parameters["userId"]
            val thoughtId = call.parameters["thoughtId"]?.toIntOrNull()
            if (userId == null || thoughtId == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID or thought ID")
                return@post
            }
            Database.downvoteThought(thoughtId, userId)
            call.respond(HttpStatusCode.OK)
        }
    }
}
