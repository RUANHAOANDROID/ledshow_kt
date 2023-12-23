import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.db.DAOImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import onbon.bx06.Bx6GEnv
import onbon.bx06.Bx6GScreenClient
import onbon.bx06.area.TextCaptionBxArea
import onbon.bx06.area.page.TextBxPage
import onbon.bx06.file.ProgramBxFile
import onbon.bx06.series.Bx6M
import onbon.bx06.utils.DisplayStyleFactory
import java.awt.Font

@Composable
@Preview
fun App() {
    var inCount by remember { mutableStateOf("X") }
    var outCount by remember { mutableStateOf("X") }
    val dao = DAOImpl
    // 启动后台任务
    DisposableEffect(Unit) {
        val webServerJob = CoroutineScope(Dispatchers.Default).launch {
            embeddedServer(Netty, 8080) {
                routing {
                    get("/count") {
                        dao.addCount("abc", 1)
                        call.respondText("Hello, world!", ContentType.Text.Html)
                    }
                    get("all") {
                        val list = dao.getAll()
                        call.respondText("${list.get(0).id}", ContentType.Text.Html)
                    }
                }
            }.start(wait = true)
        }
        Bx6GEnv.initial()
        val screen = Bx6GScreenClient("MyScreen", Bx6M())
        screen.connect("192.168.8.199", 5005)
        // 当组件被清理时，取消后台任务
        val timerJob = CoroutineScope(Dispatchers.Default).launch {
            var inCountInt =100
            var outCountInt =80
            while (true) {
                delay(1000)
                inCountInt++
                outCountInt++
                try {
                    inCount ="$inCountInt"
                    outCount ="$outCountInt "
                    //screen.turnOn()
                    val styles: List<DisplayStyleFactory.DisplayStyle> = DisplayStyleFactory.getStyles().toList()
                    val pf = ProgramBxFile("P000", screen.profile)
                    val area = TextCaptionBxArea(0, 0, 32, 16, screen.profile)
                    val page = TextBxPage("$inCountInt")
//                    page.newLine("6688")
//                    page.newLine("人")
                    page.font = Font("宋体", Font.PLAIN, 14)
                    page.displayStyle = styles[2]
                    area.addPage(page)
                    pf.addArea(area)
                    screen.writeProgram(pf)
//                    delay(1000)
//                    screen.turnOff()
//                    screen.disconnect()
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }
        onDispose {
            webServerJob.cancel()
            timerJob.cancel()
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
//                Row {
//                    Text("当前入园", fontSize = 29.sp)
//                    Text(inCount, fontSize = 29.sp, color = Color.Red)
//                    Text("人", fontSize = 29.sp)
//                }
                Row {
                    Text("当前在园", fontSize = 29.sp)
                    Text(inCount, fontSize = 29.sp, color = Color.Red)
                    Text("人", fontSize = 29.sp)
                }
            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
