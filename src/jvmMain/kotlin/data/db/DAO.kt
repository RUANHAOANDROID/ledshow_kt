package data.db

import data.db.entity.CountTable
import data.db.entity.DBMaxCount

interface DAO {
    suspend fun setup()
    suspend fun addCount(deviceId: String, inOutType: Int)
    suspend fun setMaxCount(maxCount: Int=100000)
    suspend fun getMaxCount(): Int
    suspend fun getInCount(): Int
    suspend fun getOutCount(): Int
    suspend fun getExistCount(): Int
    suspend fun getAll(): MutableList<CountTable>
}