package job

import com.typesafe.config.ConfigException.Null
import data.db.DAO
import data.db.DAOImpl
import data.model.RespError
import data.model.RespSuccess
import data.model.Response
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class WebServer {
    private var dao: DAO = DAOImpl

    suspend fun startServer(port: Int = 8080) {
        embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/passGate/{id}/{type}") {
                    val deviceId = call.parameters["id"]
                    val inOutType = call.parameters["type"]?.toInt()
                    if (deviceId==null||inOutType==null){
                        call.respond(HttpStatusCode.InternalServerError,RespError(msg = "参数错误"))
                    }
                    dao.addCount(deviceId!!, inOutType!!)
                    call.respond(HttpStatusCode.OK,RespSuccess())
                }
                get("/inCount") {
                    val inCount = dao.getInCount()
                    call.respond(HttpStatusCode.OK,RespSuccess(data = inCount))
                }
                get("/outCount") {
                    val outCount = dao.getOutCount()
                    call.respond(HttpStatusCode.OK,RespSuccess(data = outCount))
                }
                get("/existCount") {
                    val existCount = dao.getExistCount()
                    call.respond(HttpStatusCode.OK,RespSuccess(data = existCount))
                }
            }
        }.start(wait = true)
    }
}