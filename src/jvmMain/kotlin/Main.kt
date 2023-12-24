import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.db.DAO
import data.db.DAOImpl
import job.LedShow
import job.WebServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var inCount by remember { mutableStateOf("X") }
    var outCount by remember { mutableStateOf("X") }
    var ledState by remember { mutableStateOf("STATUS") }
    var ledErrorInfo by remember { mutableStateOf("") }
    var dao: DAO = DAOImpl
    // 启动后台任务
    DisposableEffect(Unit) {

        val webServerJob = CoroutineScope(Dispatchers.Default).launch {
            dao.setup()
            WebServer().startServer()
        }

        val ledJob = CoroutineScope(Dispatchers.Default).launch {
            if (LedShow.setup()) {
                ledState = "连接成功"
            } else {
                ledState = "连接失败"
            }
            LedShow.start()
            webServerJob.join()
        }
        onDispose {
            webServerJob.cancel()
            ledJob.cancel()
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
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Text("LED:", fontSize = 29.sp)
                    Text(ledState, fontSize = 29.sp, color = Color.Red)
                }
                Text("$ledErrorInfo", fontSize = 29.sp)
            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
