import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import data.model.LedParameters
import job.LedShow
import job.LedShow2
import job.WebServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var maxCount by remember { mutableStateOf("100000") }
    var existsCount by remember { mutableStateOf("X") }
    var inCount by remember { mutableStateOf("X") }
    var ledState1 by remember { mutableStateOf("STATUS") }
    var ledState2 by remember { mutableStateOf("STATUS") }
    var runInfo by remember { mutableStateOf("") }
    var ledAddress1 by remember { mutableStateOf("192.168.0.0") }
    var ledAddress2 by remember { mutableStateOf("192.168.0.0") }
    var dao: DAO = DAOImpl
    val coroutineScope = rememberCoroutineScope()
    val ledParameters1 by remember { mutableStateOf(LedParameters()) }
    val ledParameters2 by remember { mutableStateOf(LedParameters()) }
    val config by lazy { ConfigManager.loadConfig() }

    // 启动后台任务
    DisposableEffect(Unit) {
        val webServerJob = coroutineScope.launch(Dispatchers.IO) {
            ledAddress1=config.ledIp1
            ledAddress2=config.ledIp2
            runInfo = "初始化数据库"
            dao.setup()
            maxCount = dao.getMaxCount().toString()
            existsCount = dao.getExistCount().toString()
            runInfo = "开启服务"
            WebServer().startServer {
                runInfo = it
            }
        }

        val ledJob1 = coroutineScope.launch(Dispatchers.Default) {
            runCatching {
                ledState1 = "初始化连接"
                if (LedShow.setup(ledAddress1)) {
                    ledState1 = "连接成功"
                } else {
                    ledState1 = "连接失败"
                }
                LedShow.start(countCall = { e, i ->
                    existsCount = e
                    inCount = i
                }, errCall = {
                    ledState1 = it
                })
            }.onFailure {
                ledState1 = "发生异常: ${it.localizedMessage}"
            }
            webServerJob.join()
        }
        val ledJob2 = coroutineScope.launch(Dispatchers.Default) {
            runCatching {
                ledState2 = "初始化连接"
                if (LedShow2.setup(ledAddress2)) {
                    ledState2 = "连接成功"
                } else {
                    ledState2 = "连接失败"
                }
                LedShow2.start(countCall = { e, i ->
                    existsCount = e
                    inCount = i
                }, errCall = {
                    ledState2 = it
                })
            }.onFailure {
                ledState2 = "发生异常: ${it.localizedMessage}"
            }
            webServerJob.join()
        }
        onDispose {
            webServerJob.cancel()
            ledJob1.cancel()
            ledJob2.cancel()
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text("最大人数限定", fontSize = 28.sp)
                Row {
                    OutlinedTextField(maxCount, onValueChange = { input ->
                        maxCount = input
                    }, modifier = Modifier.width(150.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        maxCount.trim().toIntOrNull()?.let {
                            coroutineScope.launch(Dispatchers.IO) {
                                dao.setMaxCount(it)
                            }
                        }
                    }) {
                        Text("设定限额")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))

                Row {
                    Text("${"LED1:"}", fontSize = 28.sp)
                    Text(ledState1, fontSize = 24.sp, color = Color.Red)
                }

                Row {
                    OutlinedTextField(ledAddress1, onValueChange = { inputAddr ->
                        ledAddress1 = inputAddr
                    }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (ledAddress1.isIpAddress()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                LedShow.setParameters(ledParameters1)
                                LedShow.setup(ledAddress1)
                                ConfigManager.saveConfig(config)
                            }
                        } else {
                            ledState1 = "网络地址错误"
                        }
                    }) {
                        Text("设定LED1")
                    }
                }

                Row {
                    Text("${"LED2:"}", fontSize = 28.sp)
                    Text(ledState2, fontSize = 24.sp, color = Color.Red)
                }
                Row {
                    OutlinedTextField(ledAddress2, onValueChange = { inputAddr ->
                        ledAddress2 = inputAddr
                    }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (ledAddress2.isIpAddress()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                LedShow2.setParameters(ledParameters2)
                                LedShow2.setup(ledAddress2)
                                ConfigManager.saveConfig(config)
                            }
                        } else {
                            ledState2 = "网络地址错误"
                        }

                    }) {
                        Text("设定LED2")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Text("今日接待", fontSize = 24.sp)
                    Text(inCount, fontSize = 24.sp, color = Color.Red)
                    Text("人", fontSize = 24.sp)
                }
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