package com.github.bytecat

import com.github.bytecat.contact.Contact

interface IDebugger {

    fun onBroadcastReady()
    fun onBroadcastReceived(fromIp: String, data: ByteArray)

    fun onMessageReady()
    fun onMessageReceived(fromIp: String, data: ByteArray)

    fun onContactAdd(contact: Contact)
    fun onContactRemove(contact: Contact)

}