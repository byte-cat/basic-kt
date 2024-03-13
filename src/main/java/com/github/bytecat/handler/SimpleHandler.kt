package com.github.bytecat.handler

import java.util.concurrent.*
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy

class SimpleHandler : IHandler {

    private val executor by lazy {
        ScheduledThreadPoolExecutor(1, AbortPolicy())
    }

    override fun post(task: Runnable) {
        executor.schedule(task, 0L, TimeUnit.MILLISECONDS)
    }

    override fun post(delay: Long, task: Runnable) {
        executor.schedule(task, delay, TimeUnit.MILLISECONDS)
    }

    override fun cancel(task: Runnable) {
        executor.remove(task)
    }

    override fun shutdown() {
        executor.shutdown()
    }


}