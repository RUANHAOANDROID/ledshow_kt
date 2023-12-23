package data.db.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.dao.id.IntIdTable

const val TypeIn = 0
const val TypeOut = 1

data class CountModel(
    val id: Int,
    val day: String,
    val count: Int,
    val type: Int,
    val device_id: String
)

object CountTable : IntIdTable("count_tab") {
    val day: Column<String> = varchar("day", 30)
    val deviceId: Column<String> = varchar("device_id", 50)
    val count: Column<Int> = integer("count").default(0)
    val type: Column<Int> = integer("type")
}

class DBCount(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DBCount>(CountTable)

    var day by CountTable.day
    var deviceId by CountTable.deviceId
    var count by CountTable.count
    var type by CountTable.type
}