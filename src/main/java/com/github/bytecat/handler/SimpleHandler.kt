package com.github.bytecat.handler

import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

class SimpleHandler : IHandler {

    private val executor by lazy { Executors.newSingleThreadExecutor() }

    private val blockingQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()

    fun start() {
        executor.execute {
            while (true) {
                val task = blockingQueue.take()
                task.run()
            }
        }
    }

    override fun post(task: Runnable) {
        blockingQueue.put(task)
    }
}