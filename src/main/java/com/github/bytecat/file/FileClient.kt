package com.github.bytecat.file

import com.github.bytecat.worker.Worker
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket

class FileClient(private val worker: Worker) {

    private lateinit var clientSocket: Socket

    fun start(ip: String, port: Int) {
        clientSocket = Socket(InetAddress.getByName(ip), port)
    }

    fun sendFile(file: IFile, acceptCode: String) {
        worker.queueWork {
            val outStream = DataOutputStream(clientSocket.getOutputStream())

            // Write header info
            outStream.writeInt(acceptCode.length)
            outStream.writeBytes(acceptCode)
            outStream.writeLong(file.length())

            val inStream = file.openReadStream()
            val byteBuffer = ByteArray(8092)
            var readCount: Int
            while (inStream.read(byteBuffer).also { readCount = it } != -1) {
                outStream.write(byteBuffer, 0, readCount)
            }
            inStream.close()
            outStream.flush()
            outStream.close()
        }

    }

    fun close() {
        clientSocket.close()
    }

}