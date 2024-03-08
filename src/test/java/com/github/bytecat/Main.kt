package com.github.bytecat

import com.github.bytecat.contact.Cat

val byteCat = object : ByteCat() {
    override val debugger: IDebugger = object : IDebugger {
        override fun onBroadcastReady() {
            println("onBroadcastReady")
        }

        override fun onBroadcastReceived(fromIp: String, data: ByteArray) {
            println("onBroadcastReceived fromIp=$fromIp")
        }

        override fun onMessageReady() {
            println("onMessageReady")
        }

        override fun onMessageReceived(fromIp: String, data: ByteArray) {
            println("onMessageReceived fromIp=$fromIp")
        }

        override fun onContactAdd(cat: Cat) {
            println("onContactAdd")
        }

        override fun onContactRemove(cat: Cat) {
            println("onContactRemove")
        }

    }
}

fun main(vararg args: String) {
    byteCat.startup()
}
