package com.github.bytecat.handler

interface IHandler {
    fun post(task: Runnable)
    fun post(delay: Long, task: Runnable)
    fun cancel(task: Runnable)
}