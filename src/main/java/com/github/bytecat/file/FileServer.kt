package com.github.bytecat.file

import com.github.bytecat.worker.Worker
import java.io.File
import java.net.ServerSocket

class FileServer private constructor(private val worker: Worker) {

    companion object {

        private val START_PORT = 10000
        private val END_PORT = 11000

        fun obtain(worker: Worker): FileServer {
            val fileServer = FileServer(worker)
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

    private val fileInfoMap = HashMap<String, FileInfo>()

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

    fun waitFile(onNew: () -> File) {
        if (isWaiting) {
            return
        }
        worker.queueWork {
            isWaiting = true
            while (started) {
                val clientSocket = serverSocket.accept()
                val transfer = FileTransfer(this, clientSocket)
                transfer.callback = object : FileTransfer.Callback {
                    override fun onStart(totalSize: Long) {
                        println("totalSize=${totalSize}")
                    }

                    override fun onTransfer(receivedSize: Long, totalSize: Long) {
                        println("receivedSize=$receivedSize totalSize=${totalSize} percent=${receivedSize.toDouble() / totalSize * 100}")
                    }

                    override fun onEnd(md5: String) {
                        println("md5=${md5}")
                    }
                }
                transfer.saveTo = onNew.invoke()
                worker.queueWork(transfer)
            }
            isWaiting = false
        }
    }

    fun addFileInfo(acceptCode: String, fileName: String, length: Long, md5: String) {
        fileInfoMap[acceptCode] = FileInfo(fileName, length, md5)
    }

    fun getFileInfo(acceptCode: String): FileInfo? = fileInfoMap[acceptCode]

    fun removeFileInfo(acceptCode: String) {
        fileInfoMap.remove(acceptCode)
    }

    fun close() {
        started = false
        serverSocket.close()
    }

    data class FileInfo(val name: String, val length: Long, val md5: String)

}