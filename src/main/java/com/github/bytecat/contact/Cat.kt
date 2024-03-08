package com.github.bytecat.contact

import com.github.bytecat.Platform

data class Cat(
    val ipAddress: String,
    val name: String,
    val system: String,
    val broadcastPort: Int,
    val messagePort: Int
) {

    val platform: Platform get() {
        return when {
            system.contains("Mac OS X") -> Platform.Mac
            system.contains("iOS") -> Platform.IPhone
            system.contains("Android") -> Platform.Android
            system.contains("Windows") -> Platform.PC
            else -> Platform.Other
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Cat && other.ipAddress == ipAddress
    }

    override fun hashCode(): Int {
        return ipAddress.hashCode()
    }
}