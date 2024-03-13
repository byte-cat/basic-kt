package com.github.bytecat.ext

import org.junit.jupiter.api.Test
import java.io.File

class _TestFile {
    @Test
    fun testGetMD5() {
        val file = File(".")
        println(file.listFiles()?.joinToString {
            it.name
        })

        file.listFiles()?.filter {
            it.isFile
        }?.forEach {
            val start = System.currentTimeMillis()
            val md5 = it.getMD5()
            val cost = System.currentTimeMillis() - start
            println("${it.name} md5=$md5 cost=${cost} fileSize=${it.length()}")
        }
    }
}