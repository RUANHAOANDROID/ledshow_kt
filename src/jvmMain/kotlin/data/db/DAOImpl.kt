package data.db

import androidx.compose.ui.res.useResource
import data.db.entity.CountModel
import data.db.entity.CountTable
import data.db.entity.DBCount
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
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

    override suspend fun addCount(deviceId: String, inOutType: Int) {
//        transaction(db){
//            val day ="20231223"
//            // 执行原生 SQL 语句
//            exec("INSERT INTO count_tab (day,device_id,type) VALUES ('${day}','${deviceId}','${inOutType}') ON CONFLICT(deviceId) DO UPDATE SET count = count + 1")
//        }
        transaction(db) {
            DBCount.new {
                day = "20231223"
                this.deviceId = "abc"
                type = 1
                count = 1
            }
        }
    }

    override suspend fun getInCount(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getOutCount(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getExistCount(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getAll(): List<CountModel> {
        val resultList = CountTable.selectAll().map {
            CountModel(
                it[CountTable.id].value,
                it[CountTable.day],
                it[CountTable.count],
                it[CountTable.type],
                it[CountTable.deviceId]
            )
        }
        return resultList
    }
}