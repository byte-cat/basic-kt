package com.github.bytecat.contact

import java.util.LinkedList

class CatBook {

    private val catList = LinkedList<Contact>()

    private val callbacks = LinkedList<Callback>()

    val cats: List<Contact> get() = catList

    fun addContact(name: String, system: String,
                   ip: String, broadcastPort: Int, messagePort: Int) {
        addContact(Contact(ip, name, system, broadcastPort, messagePort))
    }

    fun addContact(contact: Contact) {
        if (catList.contains(contact)) {
            catList[catList.indexOf(contact)] = contact
            callbacks.forEach {
                it.onContactUpdate(contact)
            }
            return
        }
        catList.add(contact)
        callbacks.forEach {
            it.onContactAdd(contact)
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
        fun onContactAdd(contact: Contact)
        fun onContactUpdate(contact: Contact)
        fun onContactRemove(contact: Contact)
    }

}