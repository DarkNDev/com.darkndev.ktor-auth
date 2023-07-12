package com.darkndev.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class User(
    val id: Int,
    val username: String,
    val password: String,
    val salt: String
)

object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 64)
    val password = varchar("password", 64)
    val salt = varchar("salt", 64)

    override val primaryKey = PrimaryKey(id)
}