package com.github.bytecat

import com.github.bytecat.handler.SimpleHandler

fun main(vararg args: String) {
    val handler = SimpleHandler()
    handler.post(100L) {
        println("1111")
    }
    handler.post {
        println("222")
    }
}
