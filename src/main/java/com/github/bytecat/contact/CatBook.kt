package com.github.bytecat.contact

import java.util.LinkedList

class CatBook {

    private val catList = LinkedList<Cat>()

    private val callbacks = LinkedList<Callback>()

    val cats: List<Cat> get() = catList

    fun addCat(name: String, system: String,
               ip: String, broadcastPort: Int, messagePort: Int) {
        addCat(Cat(ip, name, system, broadcastPort, messagePort))
    }

    fun addCat(cat: Cat) {
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

    fun removeCat(ip: String) {
        val index = catList.indexOfFirst {
            it.ip == ip
        }
        if (index >= 0 && index < catList.size) {
            val contact = catList.removeAt(index)
            callbacks.forEach {
                it.onContactRemove(contact)
            }
        }
    }

    fun findCatByIp(ip: String): Cat? {
        return catList.firstOrNull {
            it.ip == ip
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