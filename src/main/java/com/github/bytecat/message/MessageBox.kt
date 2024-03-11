package com.github.bytecat.message

import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.MessageData
import java.util.LinkedList

class MessageBox private constructor(private val cat: Cat) {
    companion object {

        private val boxes = HashMap<Cat, MessageBox>()

        fun obtain(cat: Cat): MessageBox {
            return boxes.getOrPut(cat) {
                MessageBox(cat)
            }
        }
    }

    val messages = ArrayList<Message<*>>()

    private val callbacks = LinkedList<Callback>()

    fun onMessageReceived(messageData: MessageData) {
        val message = Message.fromReceive(messageData)
        messages.add(message)
        callbacks.forEach {
            it.onMessageReceived(cat, message)
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
        fun onMessageReceived(cat: Cat, message: Message<*>)
    }

}