package job

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
import kotlinx.serialization.json.Json

class WebServer {
    private var dao: DAO = DAOImpl

    suspend fun startServer(port: Int = 8888) {
        embeddedServer(Netty, port) {
            install(ContentNegotiation) {
                json()
            }
            routing {
                get("/passGate/{id}/{type}") {
                    val deviceId = call.parameters["id"]
                    val inOutType = call.parameters["type"]?.toInt()
                    if (deviceId==null||inOutType==null){
                        call.respond(RespError(msg = "参数错误"))
                    }
                    dao.addCount(deviceId!!, inOutType!!)
                    call.respond(RespSuccess(data = "aaa"))
                }
                get("/inCount") {
                    val inCount = dao.getInCount()
                    call.respondText("$inCount", ContentType.Text.Html)
                }
                get("/outCount") {
                    val outCount = dao.getOutCount()
                    call.respondText("$outCount", ContentType.Text.Html)
                }
                get("/existCount") {
                    val existCount = dao.getExistCount()
                    call.respondText("$existCount", ContentType.Text.Html)
                }
            }
        }.start(wait = true)
    }
}