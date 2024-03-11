package com.github.bytecat.message

import com.github.bytecat.protocol.data.Data

class Message<T : Data>(val type: Int, val data: T, val timestamp: Long) {

    companion object {
        const val TYPE_R = 0
        const val TYPE_S = 1

        fun <T : Data> fromReceive(data: T): Message<T> {
            return Message(TYPE_R, data, System.currentTimeMillis())
        }

    }

}