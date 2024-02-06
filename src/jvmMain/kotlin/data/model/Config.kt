package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val macCount: Int = 10000,
    val ledIp1: String = "192.168.9.1",
    val ledIp2: String = "192.168.9.1",
    val led1x:Int=0,
    val led2x:Int=0,
    val led1y:Int=0,
    val led2y:Int=0,
    val led1w:Int=48,
    val led2w:Int=48,
    val led1h:Int=128,
    val led2h:Int=128,
    val led1fs:Int=14,
    val led2fs:Int=14,
)