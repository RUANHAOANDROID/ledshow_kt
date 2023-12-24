package data.db

import androidx.compose.ui.res.useResource
import data.db.entity.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import utils.TimeUtils
import java.io.File

object DAOImpl : DAO {

    private val db by lazy {
        val file = File("data.db")
        if (!file.exists()) {
            file.createNewFile()
            useResource("data.db") {
                it.copyTo(file.outputStream())
            }
        }
        val db = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")

        transaction(db) {
            addLogger(StdOutSqlLogger)
            if (!SchemaUtils.checkCycle(CountTable)) {
                SchemaUtils.create(CountTable)
            }
        }
        db
    }

    override suspend fun setup() {
        println(db.url)
    }

    override suspend fun addCount(deviceId: String, inOutType: Int) {
//        transaction(db){
//            val day ="20231223"
//            // 执行原生 SQL 语句
//            exec("INSERT INTO count_tab (day,device_id,type) VALUES ('${day}','${deviceId}','${inOutType}') ON CONFLICT(deviceId) DO UPDATE SET count = count + 1")
//        }
        val today = TimeUtils.getToday()
        transaction(db) {
            val row = CountTable.select {
                (CountTable.day eq today) and (CountTable.deviceId eq deviceId)
            }.singleOrNull()
            if (null == row) {
                DBCount.new {
                    day = today
                    this.deviceId = deviceId
                    type = inOutType
                    count = 1
                }
            } else {
                val id = row[CountTable.id]
                val count = row[CountTable.count] + 1
                CountTable.update({ CountTable.id eq id }) {
                    it[CountTable.count] = count
                }
            }
        }
    }

    override suspend fun getInCount(): Int {
        var count = 0
        transaction(db) {
            exec("SELECT SUM(c.count) FROM count_tab c WHERE c.type =${TypeIn}") {
                if (it.next()) {
                    count = it.getInt(1)
                }
            }
        }
        return count
    }

    override suspend fun getOutCount(): Int {
        var count = 0
        transaction(db) {
            exec("SELECT SUM(c.count) FROM count_tab c WHERE c.type =${TypeOut}") {
                if (it.next()) {
                    count = it.getInt(1)
                }
            }
        }
        return count
    }

    override suspend fun getExistCount(): Int {
        return getInCount() - getOutCount()
    }

    override suspend fun getAll(): MutableList<CountTable> {
        var list = emptyList<CountTable>()
        transaction(db) {
            return@transaction CountTable.selectAll().toList<ResultRow>().toMutableList<ResultRow>()
        }
        return list.toMutableList()
    }
}