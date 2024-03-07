package com.github.bytecat

import java.net.Inet4Address
import java.net.NetworkInterface

fun main(vararg args: String) {
    println("ip=${getLocalIP()}")
}

fun getLocalIP(): String {
    try {
        for (intf in NetworkInterface.getNetworkInterfaces()) {
            for (inetAddress in intf.inetAddresses) {
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress
                }
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return "0.0.0.0"
}
