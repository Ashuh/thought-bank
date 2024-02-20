package com.ashuh.thoughtbank.database.entity

import com.ashuh.thoughtbank.database.UserSeenThoughtsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserSeenThoughtEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserSeenThoughtEntity>(UserSeenThoughtsTable)

    var user by UserEntity referencedOn UserSeenThoughtsTable.userId
    var thought by ThoughtEntity referencedOn UserSeenThoughtsTable.thoughtId
    var isUpvoted by UserSeenThoughtsTable.isUpvoted
}
