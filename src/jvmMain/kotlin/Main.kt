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
import data.model.Config
import data.model.LED
import data.model.LedParameters
import job.LedShow
import job.WebServer
import kotlinx.coroutines.*
import utils.isIpAddress
import javax.swing.JOptionPane

@Composable
@Preview
fun App() {
    var maxCount by remember { mutableStateOf("1000") }
    var existsCount by remember { mutableStateOf("X") }
    var inCount by remember { mutableStateOf("X") }
    var ledState1 by remember { mutableStateOf("STATUS") }
    var ledState2 by remember { mutableStateOf("STATUS") }
    var runInfo by remember { mutableStateOf("") }

    var dao: DAO = DAOImpl
    val coroutineScope = rememberCoroutineScope()
    val config by lazy { ConfigManager.loadConfig() }
    val ledShow1 = LedShow(LedParameters())
    val ledShow2 = LedShow(LedParameters())
    val ledDevices = remember { mutableListOf<LedShow>(ledShow2) }
//    config.leds.forEach {
//        val parameters = LedParameters().apply {
//            ip = it.ip
//            x = it.x
//            y = it.y
//            width = it.w
//            height = it.h
//            fontSize = it.fs
//        }
//        ledDevices.add(LedShow(parameters))
//    }
    var counterJob: Job? = null
    // 启动后台任务
    val led1 = config.leds[0]
    with(ledShow1.ledParameters) {
        ip = led1.ip
        x = led1.x
        y = led1.y
        width = led1.w
        height = led1.h
        fontSize = led1.fs
    }
    val led2 = config.leds[1]
    with(ledShow2.ledParameters) {
        ip = led2.ip
        x = led2.x
        y = led2.y
        width = led2.w
        height = led2.h
        fontSize = led2.fs
    }
    var ledAddress1 by remember { mutableStateOf(led1.ip) }
    var ledAddress2 by remember { mutableStateOf(led2.ip) }
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
        onDispose {
            webServerJob.cancel()
            counterJob?.cancel()
            coroutineScope.cancel()
        }
    }
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            with(ledShow1) {
                connect {
                    ledState1 = it
                }
            }
            with(ledShow2) {
                connect {
                    ledState2 = it
                }
            }
        }
        counterJob = coroutineScope.launch(Dispatchers.Default) {
            while (true) {
                //间歇1秒
                delay(2000)
                var existCountDB = dao.getExistCount()
                val inCountDB = dao.getInCount()
                if (existCountDB < 0)
                    existCountDB = 0
                existsCount = existCountDB.toString()
                inCount = inCountDB.toString()
                with(ledShow1) {
                    if (connected) {
                        setLedContent(existCountDB, inCountDB) {
                            ledState1 = it
                        }
                    }
                }
                with(ledShow2) {
                    if (connected) {
                        setLedContent(existCountDB, inCountDB) {
                            ledState2 = it
                        }
                    }
                }
                delay(2000)
            }
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
                    Text("${config.leds[0].title}", fontSize = 28.sp)
                    Text(ledState1, fontSize = 24.sp, color = Color.Red)
                }

                Row {
                    OutlinedTextField(ledAddress1, onValueChange = { inputAddr ->
                        ledAddress1 = inputAddr
                    }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (ledAddress1.isIpAddress()) {
                            coroutineScope.launch {
                                ledShow1.reconnect { ledState1 = it }
                            }
                        } else {
                            ledState1 = "网络地址错误"
                        }
                    }) {
                        Text("LED重连")
                    }
                }
                //Text("${ledShow1.ledParameters}")
                Row {
                    Text("${config.leds[1].title}", fontSize = 28.sp)
                    Text(ledState2, fontSize = 24.sp, color = Color.Red)
                }
                Row {
                    OutlinedTextField(ledAddress2, onValueChange = { inputAddr ->
                        ledAddress2 = inputAddr
                    }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (ledAddress2.isIpAddress()) {
                            coroutineScope.launch() {
                                ledShow2.reconnect {
                                    ledState2 = it
                                }
                            }
                        } else {
                            ledState2 = "网络地址错误"
                        }
                    }) {
                        Text("LED重连")
                    }
                }
                //Text("${ledShow1.ledParameters}")
                Spacer(modifier = Modifier.height(32.dp))
                Row {
                    Text("今日接待", fontSize = 24.sp)
                    Text(inCount, fontSize = 24.sp, color = Color.Red)
                }
                Row {
                    Text("当前在园", fontSize = 24.sp)
                    Text(existsCount, fontSize = 24.sp, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text("$runInfo", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun LedItem(led: LED, status: String, ledShow: LedShow, coroutineScope: CoroutineScope) {
    var ledIp = remember { led.ip }
    var status = remember { status }
    Column {
        Row {
            Text(led.title, fontSize = 28.sp)
            Text(status, fontSize = 24.sp, color = Color.Red)
        }
        Row {
            OutlinedTextField(ledIp, onValueChange = { inputAddr ->
                ledIp = inputAddr
            }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {
                if (ledIp.isIpAddress()) {
                    coroutineScope.launch() {
                        ledShow.reconnect {
                            status = it
                        }
                    }
                } else {
                    status = "网络地址错误"
                }
            }) {
                Text("LED重连")
            }
        }

    }
}

//exitApplication
fun main() = application {
    var shouldExit by remember { mutableStateOf(false) }
    Window(onCloseRequest = {
        val result = JOptionPane.showConfirmDialog(null, "是否需要关闭该程序?", "确认关闭", JOptionPane.YES_NO_OPTION)
        if (result == JOptionPane.YES_OPTION) {
            shouldExit = true
            exitApplication()
        }
    }) {
        App()
    }
}
