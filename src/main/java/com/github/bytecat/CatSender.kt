package com.github.bytecat

import com.github.bytecat.protocol.Event
import com.github.bytecat.protocol.Protocol
import com.github.bytecat.udp.UDPSender
import org.json.JSONObject

class CatSender : UDPSender() {
    fun send(ip: String, port: Int, json: JSONObject) {
        send(ip, port, json.toString())
    }

    fun send(ip: String, port: Int, event: Event) {
        send(ip, port, event.toJSONObject())
    }

    fun sendText(ip: String, port: Int, text: String) {
        send(ip, port, Protocol.text(text))
    }

}