package job

import data.db.DAO
import data.db.DAOImpl
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
                json(Json {
                    useArrayPolymorphism =true
                    allowStructuredMapKeys =true
                    prettyPrint=true
                    isLenient=true
                })
            }
            routing {
                get("/passGate/{id}/{type}") {
                    val deviceId = call.parameters["id"]
                    val inOutType = call.parameters["type"]?.toInt()
                    if (deviceId==null||inOutType==null){
                        call.respondText("Hello, world!", ContentType.Text.Html)
                    }
                    dao.addCount(deviceId!!, inOutType!!)
                    call.respondText("Hello, world!", ContentType.Text.Html)
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