package com.github.bytecat

fun main(vararg args: String) {
    println("[${System.getenv().keys.joinToString()}]")
    println(System.getenv()["USER"])
    val properties = System.getProperties()
    println(properties.getProperty("os.name"))
    println(properties.getProperty("os.arch"))
    println(properties.getProperty("os.version"))
}