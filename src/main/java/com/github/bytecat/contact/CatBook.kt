package com.github.bytecat.contact

import java.util.LinkedList

class CatBook {

    private val catList = LinkedList<Cat>()

    private val callbacks = LinkedList<Callback>()

    val cats: List<Cat> get() = catList

    fun addContact(name: String, system: String,
                   ip: String, broadcastPort: Int, messagePort: Int) {
        addContact(Cat(ip, name, system, broadcastPort, messagePort))
    }

    fun addContact(cat: Cat) {
        if (catList.contains(cat)) {
            catList[catList.indexOf(cat)] = cat
            callbacks.forEach {
                it.onContactUpdate(cat)
            }
            return
        }
        catList.add(cat)
        callbacks.forEach {
            it.onContactAdd(cat)
        }
    }

    fun removeContact(ip: String) {
        val index = catList.indexOfFirst {
            it.ipAddress == ip
        }
        if (index >= 0 && index < catList.size) {
            val contact = catList.removeAt(index)
            callbacks.forEach {
                it.onContactRemove(contact)
            }
        }
    }

    fun findByIp(ip: String): Cat? {
        return catList.firstOrNull {
            it.ipAddress == ip
        }
    }

    fun broadcastPorts(): List<Int> {
        return catList.map {
            it.broadcastPort
        }.distinct()
    }

    fun registerCallback (callback: Callback) {
        if (callbacks.contains(callback)) {
            return
        }
        callbacks.add(callback)
    }

    fun unregisterCallback (callback: Callback) {
        callbacks.remove(callback)
    }

    interface Callback {
        fun onContactAdd(cat: Cat)
        fun onContactUpdate(cat: Cat)
        fun onContactRemove(cat: Cat)
    }

}