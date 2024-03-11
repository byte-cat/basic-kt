package com.github.bytecat.contact

import com.github.bytecat.Platform

open class Cat(
    protected var ip: String,
    protected val name: String,
    protected val system: String,
    protected val broadcastPort: Int,
    protected val messagePort: Int
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
        return other is Cat && other.ip == ip
    }

    override fun hashCode(): Int {
        return ip.hashCode()
    }
}