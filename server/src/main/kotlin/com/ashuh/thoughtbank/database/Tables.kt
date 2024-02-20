package com.ashuh.thoughtbank.database

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object UsersTable : IdTable<String>() {
    override val id = varchar("id", 50).entityId().uniqueIndex()
    val name = varchar("name", 50).uniqueIndex()
}

object ThoughtsTable : IntIdTable() {
    val content = text("content")
    val author = reference("author", UsersTable.name)
    val upvotes = integer("upvotes")
    val downvotes = integer("downvotes")
    val createdOn = timestampWithTimeZone("created_on")
}

object UserSeenThoughtsTable : IntIdTable() {
    val userId = reference("userId", UsersTable.id)
    val thoughtId = reference("thoughtId", ThoughtsTable.id)
    val isUpvoted = bool("is_upvoted")

    init {
        uniqueIndex(userId, thoughtId)
    }
}
