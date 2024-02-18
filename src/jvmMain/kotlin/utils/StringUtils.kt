package utils

import java.net.InetAddress
import java.net.NetworkInterface

fun String.isIpAddress(): Boolean {
    val ipAddressRegex =
        """^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$""".toRegex()

    return ipAddressRegex.matches(this)
}


fun String.getLocalIpv4Address(): String{
    var ipAddress: String = this
    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
    while (networkInterfaces.hasMoreElements()) {
        val networkInterface = networkInterfaces.nextElement()
        val interfaceAddresses = networkInterface.inetAddresses
        while (interfaceAddresses.hasMoreElements()) {
            val inetAddress = interfaceAddresses.nextElement()
            if (!inetAddress.isLoopbackAddress && inetAddress is InetAddress) {
                if (inetAddress.hostAddress.contains(".")) { // 过滤掉 IPv6 地址
                    ipAddress = inetAddress.hostAddress
                }
            }
        }
    }
    return ipAddress
}