package com.github.bytecat.contact

import com.github.bytecat.Platform

open class Cat(ip: String, name: String, system: String, broadcastPort: Int, messagePort: Int) {

    var ip: String = ip
        protected set
    var name: String = name
        protected set
    var system: String = system
        protected set

    var broadcastPort: Int = broadcastPort
        protected set

    var messagePort: Int = messagePort
        protected set

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