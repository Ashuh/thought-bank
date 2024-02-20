package com.ashuh.thoughtbank.database.entity

import com.ashuh.thoughtbank.database.UsersTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class UserEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UserEntity>(UsersTable)

    var firebaseId by UsersTable.id
    var name by UsersTable.name
}
