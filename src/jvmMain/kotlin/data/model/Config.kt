package data.model

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val macCount: Int,
    val leds: List<LED>
)
@Serializable
data class LED(
    val title:String,
    val ip: String,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val fs: Int
)
