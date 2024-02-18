package data.db.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object LedTable : IntIdTable("count_tab") {
    val title: Column<String> = varchar("title", 50)
    val ip: Column<String> = varchar("ip", 50)
    val port: Column<Int> = integer("port").default(5005)
    val x: Column<Int> = integer("x")
    val y: Column<Int> = integer("y")
    val w: Column<Int> = integer("w")
    val h: Column<Int> = integer("h")
    val fs: Column<Int> = integer("fs")
}

class DBLed(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DBLed>(LedTable)

    var title by LedTable.title
    var ip by LedTable.ip
    var port by LedTable.port
    var x by LedTable.x
    var y by LedTable.y
    var w by LedTable.w
    var h by LedTable.h
    var fs by LedTable.fs
}