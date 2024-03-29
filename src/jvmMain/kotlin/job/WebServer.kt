package job

import data.db.DAO
import data.db.DAOImpl
import data.model.RespError
import data.model.RespSuccess
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import utils.getLocalIpv4Address

val IN = 0
val OUT = 1

class WebServer {
    private var dao: DAO = DAOImpl
    private val mutex = Mutex()
    suspend fun startServer(port: Int = 8080, callInfo: (String) -> Unit) {
        callInfo("startServer $port")
        embeddedServer(Netty, port) {
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
                get("/canEnter") {
                    mutex.withLock {
                        val maxCount = dao.getMaxCount()
                        val existsCount = dao.getExistCount()
                        if (existsCount >= maxCount) {
                            callInfo("can enter NO!")
                            call.respond(HttpStatusCode.OK, RespSuccess(data = false))
                        } else {
                            callInfo("can enter YES! ")
                            call.respond(HttpStatusCode.OK, RespSuccess(data = true))
                        }
                    }
                }
                get("/passGate/{id}/{type}") {
                    val deviceId = call.parameters["id"]
                    val inOutType = call.parameters["type"]?.toInt()
                    mutex.withLock {
                        val maxCount = dao.getMaxCount()
                        val existsCount = dao.getExistCount()
                        if ((inOutType == IN) && (existsCount >= maxCount)) {
                            callInfo("can enter NO!")
                            call.respond(HttpStatusCode.OK, RespError(msg = "园区人数超限！"))
                        } else {
                            callInfo("pass gate ${deviceId} ${inOutType}")
                            if (deviceId == null || inOutType == null) {
                                call.respond(HttpStatusCode.InternalServerError, RespError(msg = "参数错误"))
                            }
                            if ((existsCount <= 0 )&& (inOutType == OUT)) {
                                call.respond(HttpStatusCode.OK, RespSuccess())
                                return@get
                            }
                            dao.addCount(deviceId!!, inOutType!!)
                            call.respond(HttpStatusCode.OK, RespSuccess())
                        }
                    }
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
                callInfo("路由注册完毕\n服务地址：http://${"".getLocalIpv4Address()}:8080")
            }
        }.start(wait = true)
    }
}