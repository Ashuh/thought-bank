package com.ashuh.thoughtbank.database

import Thought
import com.ashuh.thoughtbank.database.entity.ThoughtEntity
import com.ashuh.thoughtbank.database.entity.UserEntity
import com.ashuh.thoughtbank.database.entity.UserSeenThoughtEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object Database {

    fun init() {
        connectToDatabase()
        initializeDatabase()
    }

    private fun connectToDatabase() {
        Database.connect(
            System.getenv("JDBC_DATABASE_URL"),
            driver = "org.postgresql.Driver"
        )
    }

    private fun initializeDatabase() {
        transaction {
            SchemaUtils.create(UsersTable, UserSeenThoughtsTable, ThoughtsTable)
        }
    }

    fun isUsernameAvailable(username: String): Boolean {
        return transaction {
            UserEntity.find { UsersTable.name eq username }.empty()
        }
    }

    fun isUserRegistered(userId: String): Any {
        return transaction {
            UserEntity.findById(userId) != null
        }
    }

    fun addUser(userId: String, name: String) {
        transaction {
            UserEntity.new {
                this.firebaseId = EntityID(userId, UsersTable)
                this.name = name
            }
        }
    }

    fun addThought(userId: String, content: String) {
        transaction {
            ThoughtEntity.new {
                this.content = content
                this.author = UserEntity.findById(userId)!!
                this.upvotes = 0
                this.downvotes = 0
            }
        }
    }

    private fun getUserSeenThoughtIds(userId: String): List<Int> {
        val seenThoughts = mutableListOf<Int>()
        transaction {
            UserSeenThoughtEntity
                .find(UserSeenThoughtsTable.userId eq userId)
                .map { it.thought.id.value }
                .toCollection(seenThoughts)
        }
        return seenThoughts
    }

    fun getUserUnseenThoughts(userId: String, numThoughts: Int): List<Thought> {
        val unseenThoughts = mutableListOf<Thought>()
        transaction {
            val seenThoughts = getUserSeenThoughtIds(userId)
            ThoughtEntity
                .find { ThoughtsTable.id notInList seenThoughts }
                .orderBy(Pair(calculateHotness(), SortOrder.DESC))
                .limit(numThoughts)
                .map {
                    mapThoughtEntityToThought(it)
                }
                .toCollection(unseenThoughts)
        }
        return unseenThoughts
    }

    fun getUserUpvotedThoughts(userId: String): List<Thought> {
        val upvotedThoughts = mutableListOf<Thought>()
        transaction {
            UserSeenThoughtEntity
                .find { UserSeenThoughtsTable.userId eq userId and UserSeenThoughtsTable.isUpvoted eq Op.TRUE }
                .map { mapThoughtEntityToThought(it.thought) }
                .toCollection(upvotedThoughts)
        }
        return upvotedThoughts
    }

    private fun addUserSeenThought(userId: String, thoughtId: Int, isUpvoted: Boolean) {
        transaction {
            UserSeenThoughtEntity.new {
                this.user = UserEntity[userId]
                this.thought = ThoughtEntity[thoughtId]
                this.isUpvoted = isUpvoted
            }
        }
    }

    fun upvoteThought(thoughtId: Int, userId: String) {
        transaction {
            addUserSeenThought(userId, thoughtId, true)
            val thought = ThoughtEntity[thoughtId]
            thought.upvotes++
        }
    }

    fun downvoteThought(thoughtId: Int, userId: String) {
        transaction {
            addUserSeenThought(userId, thoughtId, false)
            val thought = ThoughtEntity[thoughtId]
            thought.downvotes++
        }
    }

    private fun calculateHotness(): Hotness {
        return Hotness()
    }

    private class Hotness : Function<Double>(DoubleColumnType()) {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
            append("((")
            append("LOG10(GREATEST(ABS(${ThoughtsTable.upvotes.name} - ${ThoughtsTable.downvotes.name}), 1))")
            append(" + ")
            append("SIGN(${ThoughtsTable.upvotes.name} - ${ThoughtsTable.downvotes.name})")
            append(" * ")
            append("EXTRACT(EPOCH FROM ${ThoughtsTable.createdOn.name})")
            append(" - 1134028003))")
            append(" / ")
            append("45000")
        }
    }

    private fun mapThoughtEntityToThought(thoughtEntity: ThoughtEntity): Thought {
        return Thought(
            thoughtEntity.id.value,
            thoughtEntity.content,
            thoughtEntity.author.name,
            thoughtEntity.upvotes - thoughtEntity.downvotes
        )
    }
}
