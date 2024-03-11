package com.github.bytecat.message

import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.MessageData
import java.util.LinkedList

class MessageBox private constructor() {
    companion object {

        private val boxes = HashMap<Cat, MessageBox>()

        fun obtain(cat: Cat): MessageBox {
            return boxes.getOrPut(cat) {
                MessageBox()
            }
        }
    }

    val messages = ArrayList<Message<*>>()

    private val callbacks = LinkedList<Callback>()

    fun onMessageReceived(messageData: MessageData) {
        val message = Message.fromReceive(messageData)
        messages.add(message)
        callbacks.forEach {
            it.onMessageReceived(message)
        }
    }

    fun registerCallback(callback: Callback) {
        if (callbacks.contains(callback)) {
            return
        }
        callbacks.add(callback)
    }

    fun unregisterCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    interface Callback {
        fun onMessageReceived(messages: Message<*>)
    }

}