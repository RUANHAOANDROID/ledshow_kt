package job

import data.db.DAO
import data.db.DAOImpl
import data.model.RespError
import data.model.RespSuccess
import io.ktor.http.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

class WebServer {
    private var dao: DAO = DAOImpl

    suspend fun startServer(port: Int = 8080, callInfo: (String) -> Unit) {
        callInfo("startServer $port")
        embeddedServer(Netty , port) {
            callInfo("添加JSON序列化")
            install(ContentNegotiation) {
                json()
            }
            callInfo("添加跨域支持")
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Delete)
                allowMethod(HttpMethod.Patch)
                allowHeader(HttpHeaders.Authorization)
            }
            callInfo("注册路由")
            routing {
                get("/ping") {
                    callInfo("Ping")
                    call.respond(RespSuccess(data = "pong"))
                }
                get("/canEnter"){

                    val maxCount =dao.getMaxCount()
                    val existsCount  =dao.getExistCount()
                    if (existsCount>=maxCount){
                        callInfo("can enter NO!")
                        call.respond(HttpStatusCode.OK, RespSuccess(data = false))
                    }else {
                        callInfo("can enter YES! ")
                        call.respond(HttpStatusCode.OK, RespSuccess(data = true))
                    }
                }
                get("/passGate/{id}/{type}") {
                    val deviceId = call.parameters["id"]
                    val inOutType = call.parameters["type"]?.toInt()
                    callInfo("pass gate ${deviceId} ${inOutType}")
                    if (deviceId == null || inOutType == null) {
                        call.respond(HttpStatusCode.InternalServerError, RespError(msg = "参数错误"))
                    }
                    dao.addCount(deviceId!!, inOutType!!)
                    call.respond(HttpStatusCode.OK, RespSuccess())
                }
                get("/inCount") {
                    val inCount = dao.getInCount()
                    call.respond(HttpStatusCode.OK, RespSuccess(data = inCount))
                }
                get("/outCount") {
                    val outCount = dao.getOutCount()
                    call.respond(HttpStatusCode.OK, RespSuccess(data = outCount))
                }
                get("/existCount") {
                    val existCount = dao.getExistCount()
                    call.respond(HttpStatusCode.OK, RespSuccess(data = existCount))
                }
                callInfo("路由注册完毕,端口${port}")
            }
        }.start(wait = true)
    }
}