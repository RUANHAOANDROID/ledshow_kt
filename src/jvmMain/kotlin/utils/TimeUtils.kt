package utils

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
object TimeUtils {
    fun getToday(): String {
        // 定义日期格式
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd")
        // 格式化日期为字符串
        return LocalDate.now().toString(formatter)
    }
}