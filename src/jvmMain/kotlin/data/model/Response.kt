package data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("msg")
    val msg: String = "",
    @SerialName("data")
    @Contextual val data: T? = null,
)

fun <T> RespSuccess(msg: String = "success", data: T? = null): Response<T> {
    return Response(code = 0, msg = msg, data = data)
}
fun RespSuccess(msg: String = "success"): Response<String> {
    return Response(code = 0, msg = msg, data = null)
}
fun RespError(msg: String = "error", data: String? = null): Response<String> {
    return Response(code = 1, msg = msg, data = data)
}