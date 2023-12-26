import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.EditProcessor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.db.DAO
import data.db.DAOImpl
import job.LedShow
import job.WebServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var maxCount by remember { mutableStateOf("100000") }
    var existsCount by remember { mutableStateOf("X") }
    var ledState by remember { mutableStateOf("STATUS") }
    var runInfo by remember { mutableStateOf("") }
    var ledAddress by remember { mutableStateOf("192.168.8.199") }
    var dao: DAO = DAOImpl
    val coroutineScope = rememberCoroutineScope()
    // 启动后台任务
    DisposableEffect(Unit) {
        val webServerJob = coroutineScope.launch(Dispatchers.IO) {
            runInfo = "初始化数据库"
            dao.setup()
            maxCount = dao.getMaxCount().toString()
            existsCount = dao.getExistCount().toString()
            runInfo = "开启服务"
            WebServer().startServer {
                runInfo = it
            }
        }

        val ledJob = coroutineScope.launch(Dispatchers.Default) {
            runCatching {
                ledState = "初始化连接"
                if (LedShow.setup()) {
                    ledState = "连接成功"
                    LedShow.start(countCall = {
                        existsCount = it
                    }, errCall = {
                        ledState = it
                    })
                } else {
                    ledState = "连接失败"
                }
            }.onFailure {
                ledState = "发生异常: ${it.localizedMessage}"
            }
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
                Text("最大人数限定", fontSize = 28.sp)
                OutlinedTextField(maxCount, onValueChange = { input ->
                    maxCount = input
                    maxCount.trim().toIntOrNull()?.let {
                        coroutineScope.launch(Dispatchers.IO) {
                            dao.setMaxCount(it)
                        }
                    }
                })
                Spacer(modifier = Modifier.height(32.dp))

                Text("LED:", fontSize = 28.sp)
                Row {
                    OutlinedTextField(ledAddress, onValueChange = { inputAddr ->
                        ledAddress = inputAddr
                    })
                    Text(ledState, fontSize =24.sp, color = Color.Red)
                }
                Button(onClick = {
                    if (ledAddress.isIpAddress()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            LedShow.setup(ledAddress)
                        }
                    } else {
                        ledState = "网络地址错误"
                    }
                }) {
                    Text("设定")
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Text("当前在园", fontSize = 24.sp)
                    Text(existsCount, fontSize = 24.sp, color = Color.Red)
                    Text("人", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("$runInfo", fontSize = 24.sp)
            }

        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

fun String.isIpAddress(): Boolean {
    val ipAddressRegex =
        """^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$""".toRegex()

    return ipAddressRegex.matches(this)
}