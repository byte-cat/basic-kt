package com.github.bytecat

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UDPSender {

    var isOpen = false
        private set

    private val socket by lazy {
        isOpen = true
        DatagramSocket()
    }

    fun send(ip: String, port: Int, data: ByteArray) {
        val packet = DatagramPacket(data, data.size, InetAddress.getByName(ip), port)
        socket.broadcast = true
        socket.send(packet)
    }

    fun send(ip: String, port: Int, message: String) {
        send(ip, port, message.toByteArray())
    }

    fun close() {
        if (isOpen) {
            isOpen = false
            socket.close()
        }
    }

}