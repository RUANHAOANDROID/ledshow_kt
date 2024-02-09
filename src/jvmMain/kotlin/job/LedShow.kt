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

object LedShow {
    private val dao: DAO = DAOImpl
    private var connected = false
    private var ledParameters = LedParameters()
    private val screen by lazy {
        Bx6GEnv.initial()
        Bx6GScreenClient("MyScreen", Bx6M())
    }

    suspend fun setup(ip: String = "192.168.8.199", port: Int = 5005): Boolean {
        connected = screen.connect(ip, port)
        return connected
    }

    suspend fun start(countCall: (String, String) -> Unit, errCall: (String) -> Unit) {
        while (true) {
            //间歇1秒
            delay(1000)
            var existCount = dao.getExistCount()
            val inCount = dao.getInCount()
            if (existCount<0)
                existCount=0
            countCall("${existCount}", "${inCount}")
            if (connected) {
                setLedContent(existCount, inCount, errCall)
            }
        }
    }

    private fun setLedContent(existCount: Int, inCount: Int, errCall: (String) -> Unit) {
        showDynamicArea(inCount, existCount, errCall)
        //showStaticArea(inCount, existCount, errCall)
    }

    private fun showStaticArea(inCount: Int, existCount: Int, errCall: (String) -> Unit) {
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
            val page = TextBxPage("今日接待${inCount}人")
            page.newLine("实时园内${existCount}人")
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
//                ledParameters.height / 2,
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
//                ledParameters.height / 2,
//                ledParameters.width,
//                ledParameters.height / 2,
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
    private fun showDynamicArea(inCount: Int, existCount: Int, errCall: (String) -> Unit) {
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
            val page = TextBxPage("今日接待${inCount}人")
            page.newLine("实时园内${existCount}人")
            area.addPage(page)
            screen.writeDynamic(rule, area)
        }.onSuccess {
            errCall("设定成功")
        }.onFailure {
            errCall("${it.message}")
        }
    }


    fun setParameters(ledParameters: LedParameters) {
        this.ledParameters = ledParameters
    }

    fun show(text: String) {

    }

    fun stop() {
        screen.disconnect()
    }

}