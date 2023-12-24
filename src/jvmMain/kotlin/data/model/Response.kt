package data.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(

    @SerialName("msg")
    val msg: String = "",
    @SerialName("data")
    @Contextual val data: T? = null,
    @SerialName("code")
    val code: Int = 0,
)

fun <T> RespSuccess(msg: String = "success", data: T? = null): Response<T> {
    return Response(code = 0, msg = msg, data = data)
}

fun RespError(msg: String = "error", data: String? = null): Response<String> {
    return Response(code = 1, msg = msg, data = data)
}