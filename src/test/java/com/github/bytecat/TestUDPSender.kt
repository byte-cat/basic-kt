package com.github.bytecat

import com.github.bytecat.handler.SimpleHandler
import org.junit.jupiter.api.Test

class TestUDPSender {

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"
    }

    private val handler = SimpleHandler()

    @Test
    fun testSend() {

        val udpSender = UDPSender()
        udpSender.send(BROADCAST_IP, 8888, "TEST-AAA")
    }
}