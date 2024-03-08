package com.github.bytecat.utils

import com.github.bytecat.contact.Cat

interface IDebugger {

    fun onBroadcastReady()
    fun onBroadcastReceived(fromIp: String, data: ByteArray)

    fun onMessageReady()
    fun onMessageReceived(fromIp: String, data: ByteArray)

    fun onContactAdd(cat: Cat)
    fun onContactRemove(cat: Cat)

}