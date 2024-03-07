package com.github.bytecat.handler

interface IHandler {
    fun post(task: Runnable)
}