package com.github.bytecat.message

import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.FileRequestData
import com.github.bytecat.protocol.data.FileResponseData
import com.github.bytecat.protocol.data.TextData
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

    fun onTextReceived(textData: TextData) {
        val message = Message.fromReceive(textData)
        messages.add(message)
        callbacks.forEach {
            it.onMessageReceived(cat, message)
        }
    }

    fun onFileRequestReceived(fileReq: FileRequestData) {
        val message = Message.fromReceive(fileReq)
        messages.add(message)
        callbacks.forEach {
            it.onMessageReceived(cat, message)
        }
    }

    fun onFileResponseReceived(fileRes: FileResponseData) {
        val message = Message.fromReceive(fileRes)
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