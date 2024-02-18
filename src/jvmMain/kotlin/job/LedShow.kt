package job

import data.db.DAO
import data.db.DAOImpl
import data.model.LedParameters
import kotlinx.coroutines.delay
import onbon.bx06.Bx6GEnv
import onbon.bx06.Bx6GScreenClient
import onbon.bx06.area.DynamicBxArea
import onbon.bx06.area.TextCaptionBxArea
import onbon.bx06.area.page.TextBxPage
import onbon.bx06.cmd.dyn.DynamicBxAreaRule
import onbon.bx06.file.ProgramBxFile
import onbon.bx06.series.Bx6M
import onbon.bx06.utils.DisplayStyleFactory

class LedShow(var ledParameters: LedParameters) {
    var connected = false
    private var ip: String = "192.168.8.199"
    private var port: Int = 5005
    private val screen by lazy {
        Bx6GEnv.initial()
        Bx6GScreenClient("MyScreen", Bx6M())
    }

    suspend fun connect(statusCall: (String) -> Unit): Boolean {
        this.ip = ledParameters.ip
        this.port = ledParameters.port
        connected = screen.connect(ip, port)
        statusCall(if (connected) "连接成功" else "连接失败")
        return connected
    }

    suspend fun reconnect(errCall: (String) -> Unit) {
        errCall("正在重连.")
        delay(500)
        errCall("正在重连..")
        delay(500)
        errCall("正在重连...")
        delay(500)
        errCall("正在重连....")
        delay(500)
        connected = screen.connect(ip, port)
        if (connected) {
            errCall("连接成功")
        } else {
            errCall("连接失败")
        }
    }

    suspend fun setLedContent(existCount: Int, inCount: Int, errCall: (String) -> Unit) {
        showDynamicArea(inCount, existCount, errCall)
        //showStaticArea(inCount, existCount, errCall)
        //showDynamicAreaTest(screen, existCount, inCount, errCall)
    }


    //    private fun showDynamicArea(inCount: Int, existCount: Int, errCall: (String) -> Unit) {
//        runCatching {
//            val rule = DynamicBxAreaRule()
//            rule.id = 0
//            rule.immediatePlay = 1.toByte()
//            rule.runMode = 0.toByte()
//            val area = DynamicBxArea(
//                ledParameters.x,
//                ledParameters.y,
//                ledParameters.width,
//                ledParameters.height/2,
//                screen.profile
//            )
//            val page = TextBxPage("今日接待${inCount}人")
//            //page.newLine("实时园内${existCount}人")
//            area.addPage(page)
//            screen.writeDynamic(rule, area)
//
//            val rule2 = DynamicBxAreaRule()
//            rule.id = 1
//            rule.immediatePlay = 1.toByte()
//            rule.runMode = 0.toByte()
//            val area2 = DynamicBxArea(
//                ledParameters.x,
//                ledParameters.height/2,
//                ledParameters.width,
//                ledParameters.height/2,
//                screen.profile
//            )
//            val page2 = TextBxPage("实时园内${existCount}人")
//            area2.addPage(page2)
//            screen.writeDynamic(rule2, area2)
//        }.onSuccess {
//            errCall("设定成功")
//        }.onFailure {
//            errCall("${it.message}")
//        }
//    }
    private suspend fun showDynamicArea(inCount: Int, existCount: Int, errCall: (String) -> Unit) {
        runCatching {
            val rule = DynamicBxAreaRule()
            rule.id = 0
            rule.immediatePlay = 1.toByte()
            rule.runMode = 0.toByte()
            val area = DynamicBxArea(
                ledParameters.x,
                ledParameters.y,
                ledParameters.width,
                ledParameters.height,
                screen.profile
            )
            val page = TextBxPage("今日接待${inCount}")
            page.newLine("实时在园${existCount}")
            area.addPage(page)
            screen.writeDynamic(rule, area)
        }.onSuccess {
            val resultString = it.toString()
            errCall(resultString)
            if (resultString.contains("断线")) {
                disconnect()
                reconnect(errCall)
            }
        }.onFailure {
            errCall("${it.message}")
        }
    }

    suspend fun showStaticArea(inCount: Int, existCount: Int, errCall: (String) -> Unit) {
        try {
            //screen.turnOn()
            val styles: List<DisplayStyleFactory.DisplayStyle> = DisplayStyleFactory.getStyles().toList()
            val pf = ProgramBxFile("P000", screen.profile)
            val area = TextCaptionBxArea(
                ledParameters.x,
                ledParameters.y,
                ledParameters.width,
                ledParameters.height,
                screen.profile
            )
            val page = TextBxPage("今日接待${inCount}")
            page.newLine("实时园内${existCount}")
            //                    page.newLine("人")
            //            page.font = Font("宋体", Font.PLAIN, ledParameters.fontSize)
            page.displayStyle = styles[3]
            area.addPage(page)
            pf.addArea(area)
            screen.writeProgram(pf)
            errCall("设定成功")
            //                    delay(1000)
            //                    screen.turnOff()
            //                    screen.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            errCall(e.message.toString())
        }
    }

    suspend fun LedShow.showDynamicAreaTest(
        screen: Bx6GScreenClient,
        inCount: Int,
        existCount: Int,
        errCall: (String) -> Unit
    ) {
        runCatching {
            val rule = DynamicBxAreaRule()
            rule.id = 0
            rule.immediatePlay = 1.toByte()
            rule.runMode = 0.toByte()
            val area = DynamicBxArea(
                0,
                0,
                32,
                16,
                screen.profile
            )
            val page = TextBxPage("${inCount}")
            area.addPage(page)
            screen.writeDynamic(rule, area)
        }.onSuccess {
            val resultString = it.toString()
            errCall(resultString)
            if (resultString.contains("断线")) {
                disconnect()
                reconnect(errCall)
            }
        }.onFailure {
            errCall("${it.message}")
        }
    }

    fun disconnect() {
        connected = false
        screen.disconnect()
    }

}