package com.github.bytecat.file

import com.github.bytecat.ByteCat
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.net.Socket
import java.security.MessageDigest

class FileReceiver(
    private val myCat: ByteCat,
    private val clientSocket: Socket,
    private val outputDir: File
) : Runnable {

    var callback: TransferCallback? = null

    override fun run() {
        val inStream = DataInputStream(clientSocket.getInputStream())

        // Read header info
        val acceptCodeLen = inStream.readInt()
        val acceptCodeBytes = ByteArray(acceptCodeLen)
        inStream.read(acceptCodeBytes)
        val acceptCode = String(acceptCodeBytes)

        val registeredFileInfo = myCat.fileReceiveManager.getFileInfo(acceptCode) ?: return

        val totalSize = inStream.readLong()

        callback?.onStart(registeredFileInfo.receiveFrom, acceptCode, totalSize)

        val md5Digest = MessageDigest.getInstance("MD5")

        var receivedSize = 0L
        val byteBuffer = ByteArray(8092)
        var readSize: Int

        val outputFile = File(outputDir, registeredFileInfo.name)
        val outputFileTmp = File("${outputFile.absolutePath}.tmp")
        val outStream = FileOutputStream(outputFileTmp)

        while (inStream.read(byteBuffer).also { readSize = it } != -1) {
            outStream.write(byteBuffer, 0, readSize)
            receivedSize += readSize
            md5Digest.update(byteBuffer, 0, readSize)
            callback?.onTransfer(registeredFileInfo.receiveFrom, acceptCode, receivedSize, totalSize)
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

        myCat.fileReceiveManager.removeFileInfo(acceptCode)
        callback?.onSuccess(registeredFileInfo.receiveFrom, acceptCode, hashText)
    }

}