package com.github.bytecat.worker

import java.util.concurrent.Callable
import java.util.concurrent.Executors

class Worker {

    private val executor = Executors.newWorkStealingPool()

    fun queueWork(task: Runnable) {
        executor.execute(task)
    }

    fun <T> queueWork(callable: Callable<T>, result: (T) -> Unit) {
        executor.execute {
            val r = callable.call()
            result.invoke(r)
        }
    }

}