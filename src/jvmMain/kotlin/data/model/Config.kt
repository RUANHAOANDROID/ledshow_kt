package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    var macCount: Int = 10000,
    var ledIp1: String = "192.168.9.1",
    var ledIp2: String = "192.168.9.1",
    var led1x:Int=0,
    var led2x:Int=0,
    var led1y:Int=0,
    var led2y:Int=0,
    var led1w:Int=48,
    var led2w:Int=48,
    var led1h:Int=128,
    var led2h:Int=128,
    var led1fs:Int=14,
    var led2fs:Int=14,
)