package com.github.bytecat.file

import com.github.bytecat.worker.Worker
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket

class FileClient(private val worker: Worker) {

    private lateinit var clientSocket: Socket

    var callback: TransferCallback? = null

    fun start(ip: String, port: Int) {
        clientSocket = Socket(InetAddress.getByName(ip), port)
    }

    fun sendFile(task: FileSendManager.SendTask, acceptCode: String) {
        worker.queueWork {
            val file = task.file
            val outStream = DataOutputStream(clientSocket.getOutputStream())

            // Write header info
            outStream.writeInt(acceptCode.length)
            outStream.writeBytes(acceptCode)
            outStream.writeLong(file.length())

            val totalSize = file.length()
            callback?.onStart(task.sendTo, totalSize)

            val inStream = file.openReadStream()
            val byteBuffer = ByteArray(8092)
            var readCount: Int
            var writeSize = 0L
            while (inStream.read(byteBuffer).also { readCount = it } != -1) {
                outStream.write(byteBuffer, 0, readCount)
                writeSize += readCount

                callback?.onTransfer(task.sendTo, writeSize, totalSize)
            }
            inStream.close()
            outStream.flush()
            outStream.close()

            // TODO
            // callback?.onEnd()
        }

    }

    fun close() {
        clientSocket.close()
    }

}