package com.github.bytecat.file

import com.github.bytecat.ByteCat
import com.github.bytecat.ext.getMD5
import com.github.bytecat.worker.Worker
import org.junit.jupiter.api.Test
import java.io.File

class TestFileServer {

    private val worker = Worker()
    private val myCat = ByteCat()

    @Test
    fun testWaitFile() {
        val server = FileServer.obtain(myCat, File("./build"))
        Thread {
            Thread.sleep(1000L)
            val client = FileClient(worker)
            client.start("127.0.0.1", server.port)

            val file = DefaultFile("./transfer/lijiang_mufu.HEIC")
            println("sendFile md5=${file.getMD5()}")
//            client.sendFile(file, "12345678")
        }.start()

        server.waitFile()

        Thread.sleep(3000L)
    }
}