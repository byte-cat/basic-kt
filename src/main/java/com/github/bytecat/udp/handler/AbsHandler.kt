package com.github.bytecat.udp.handler

import com.github.bytecat.protocol.Event
import com.github.bytecat.udp.UDPReceiver
import org.json.JSONObject

abstract class AbsHandler : UDPReceiver.OnReceiveListener {
    fun dispatchReceive(data: ByteArray, handler: (event: Event) -> Unit) {
        val text = String(data)
        val jsonObj = JSONObject(text)
        handler.invoke(Event(jsonObj))
    }
}