import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var runInfo by remember { mutableStateOf("") }
    var dao: DAO = DAOImpl
    val coroutineScope = rememberCoroutineScope()
    val config by lazy { ConfigManager.loadConfig() }
    val ledDevices = remember { mutableListOf<LedShow>() }
    config.leds.forEach {
        val parameters = LedParameters().apply {
            ip = it.ip
            x = it.x
            y = it.y
            width = it.w
            height = it.h
            fontSize = it.fs
        }
        val element = LedShow(parameters)
        element.name = it.title
        ledDevices.add(element)
    }
    var counterJob: Job? = null
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
            ledDevices.forEach {
                it.connect()
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
                ledDevices.forEach {
                    with(it) {
                        if (connected) {
                            setLedContent(existCountDB, inCountDB)
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
                LazyColumn(
                    modifier = Modifier.defaultMinSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ledDevices.size) { index ->
                        var status by remember { mutableStateOf("") }
                        Text(ledDevices[index].name, fontSize = 28.sp)
                        Row {
                            var ledIp by remember { mutableStateOf(ledDevices[index].ledParameters.ip) }
                            OutlinedTextField(ledDevices[index].ledParameters.ip, onValueChange = { inputAddr ->
                                ledIp = inputAddr
                            }, label = { Text("IP address") }, modifier = Modifier.width(180.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(onClick = {
                                if (ledIp.isIpAddress()) {
                                    coroutineScope.launch {
                                        ledDevices[index].reconnect()
                                    }
                                } else {
                                    status = "网络地址错误"
                                }
                            }) {
                                Text("LED重连")
                            }
                        }
                        Text(status, fontSize = 16.sp, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                        ledDevices[index].registerStatus {
                            status = it
                        }
                    }
                }

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
