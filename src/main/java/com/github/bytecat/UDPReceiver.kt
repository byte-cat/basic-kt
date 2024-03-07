package com.github.bytecat

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.concurrent.Executor

class UDPReceiver(val port: Int, private val executor: Executor? = null, val bufferSize: Int = 1024) {

    @Volatile
    var isListening = false
        private set

    var isReady = false
        private set

    private var receiveListener: OnReceiveListener? = null

    private val task = Runnable {
        while (isListening) {
            val bytes = ByteArray(bufferSize)
            val packet = DatagramPacket(bytes, bytes.size)
            if (!isReady) {
                isReady = true
                receiveListener?.onReady()
            }
            try {
                socket.receive(packet)
            } catch (e: Exception) {
                if (isListening) {
                    e.printStackTrace()
                } else {
                    return@Runnable
                }
            }

            val address = packet.address
            if (address == null) {
                println("ERROR: packet.address is NULL")
                return@Runnable
            }

            val data = packet.data.copyOfRange(packet.offset, packet.offset + packet.length)
            receiveListener?.onReceive(packet.address.hostAddress, data)
        }
    }

    private lateinit var socket: DatagramSocket

    fun listen(listener: OnReceiveListener): Boolean {
        if (isListening) {
            receiveListener = listener
            return true
        }
        try {
            socket = DatagramSocket(port)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        isListening = true
        receiveListener = listener
        if (executor == null) {
            Thread(task).start()
        } else {
            executor.execute(task)
        }
        return true
    }

    fun close() {
        if (isListening) {
            isListening = false
            socket.close()
        }
        receiveListener = null
    }

    interface OnReceiveListener {
        fun onReady()
        fun onReceive(fromIp: String, data: ByteArray)
    }

}