package com.github.bytecat.file

import com.github.bytecat.ByteCat
import java.io.File
import java.net.ServerSocket

class FileServer private constructor(private val myCat: ByteCat, private var outputDir: File) {

    companion object {

        private val START_PORT = 10000
        private val END_PORT = 11000

        fun obtain(myCat: ByteCat, outputDir: File): FileServer {
            val fileServer = FileServer(myCat, outputDir)
            var port = START_PORT
            while (!fileServer.start(port) && port <= END_PORT) {
                port++
            }
            return fileServer
        }
    }

    private lateinit var serverSocket: ServerSocket

    @Volatile
    private var started = false

    var isWaiting = false
        private set

    val port get() = serverSocket.localPort

    fun start(port: Int): Boolean {
        try {
            serverSocket = ServerSocket(port)
            started = true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun waitFile() {
        if (isWaiting) {
            return
        }
        myCat.worker.queueWork {
            isWaiting = true
            while (started) {
                val clientSocket = serverSocket.accept()
                val transfer = FileReceiver(myCat, clientSocket, outputDir)
                myCat.worker.queueWork(transfer)
            }
            isWaiting = false
        }
    }

    fun close() {
        started = false
        serverSocket.close()
    }

}