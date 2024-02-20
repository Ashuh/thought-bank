package com.ashuh.thoughtbank.database.entity

import com.ashuh.thoughtbank.database.ThoughtsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class ThoughtEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ThoughtEntity>(ThoughtsTable)

    var content by ThoughtsTable.content
    var author by UserEntity referencedOn ThoughtsTable.author
    var upvotes by ThoughtsTable.upvotes
    var downvotes by ThoughtsTable.downvotes
    var createdOn by ThoughtsTable.createdOn
}
