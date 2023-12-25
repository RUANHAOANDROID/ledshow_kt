package job

import data.db.DAO
import data.db.DAOImpl
import kotlinx.coroutines.delay
import onbon.bx06.Bx6GEnv
import onbon.bx06.Bx6GScreenClient
import onbon.bx06.area.TextCaptionBxArea
import onbon.bx06.area.page.TextBxPage
import onbon.bx06.file.ProgramBxFile
import onbon.bx06.series.Bx6M
import onbon.bx06.utils.DisplayStyleFactory
import java.awt.Font

object LedShow {
    private var inCount = "0"
    private var outCount = "0"
    private lateinit var screen: Bx6GScreenClient
    private val dao: DAO = DAOImpl
    suspend fun setup(ip: String = "192.168.8.199", port: Int = 5005): Boolean {
        Bx6GEnv.initial()
        screen = Bx6GScreenClient("MyScreen", Bx6M())
        return screen.connect(ip, port)
    }

    suspend fun start(countCall :(String)->Unit,errCall:(String)->Unit) {
        while (true) {
            delay(1000)
            val count = dao.getExistCount()
            countCall("${count}")
            try {
                //screen.turnOn()
                val styles: List<DisplayStyleFactory.DisplayStyle> = DisplayStyleFactory.getStyles().toList()
                val pf = ProgramBxFile("P000", screen.profile)
                val area = TextCaptionBxArea(0, 0, 32, 16, screen.profile)
                val page = TextBxPage("$count")
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
                e.printStackTrace()
                errCall(e.message.toString())
            }
        }
    }

    fun show(text: String) {

    }

    fun stop() {
        screen.disconnect()
    }

}