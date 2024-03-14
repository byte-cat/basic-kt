package com.github.bytecat.file

import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.Socket
import java.security.MessageDigest

class FileTransfer(
    private val server: FileServer,
    private val clientSocket: Socket,
    private val outputDir: File
) : Runnable {

    var callback: Callback? = null

    override fun run() {
        val inStream = DataInputStream(clientSocket.getInputStream())

        // Read header info
        val acceptCodeLen = inStream.readInt()
        val acceptCodeBytes = inStream.readNBytes(acceptCodeLen)
        val acceptCode = String(acceptCodeBytes)

        val registeredFileInfo = server.getFileInfo(acceptCode)

        val totalSize = inStream.readLong()

        callback?.onStart(totalSize)

        val md5Digest = MessageDigest.getInstance("MD5")

        var receivedSize = 0L
        val byteBuffer = ByteArray(8092)
        var readSize: Int

        val outputFile = File(outputDir, registeredFileInfo!!.name)
        val outputFileTmp = File("${outputFile.absolutePath}.tmp")
        val outStream = FileOutputStream(outputFileTmp)

        while (inStream.read(byteBuffer).also { readSize = it } != -1) {
            outStream.write(byteBuffer, 0, readSize)
            receivedSize += readSize
            md5Digest.update(byteBuffer, 0, readSize)
            callback?.onTransfer(receivedSize, totalSize)
        }

        inStream.close()
        outStream.flush()
        outStream.close()

        outputFileTmp.renameTo(outputFile)

        val bigInt = BigInteger(1, md5Digest.digest())
        val hashText = bigInt.toString(16).run {
            if (length < 32) {
                val sb = StringBuilder(this)
                val pre = CharArray(32 - this.length) {
                    '0'
                }
                sb.insert(0, pre).toString()
            } else {
                this
            }
        }

        callback?.onEnd(outputFile, hashText, acceptCode)
    }

    interface Callback {
        fun onStart(totalSize: Long)
        fun onTransfer(receivedSize: Long, totalSize: Long)
        fun onEnd(file: File, md5: String, acceptCode: String)
    }

}