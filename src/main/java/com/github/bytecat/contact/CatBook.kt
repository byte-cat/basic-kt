package com.github.bytecat.contact

import java.util.LinkedList

class CatBook {

    private val contacts = LinkedList<Contact>()

    private val callbacks = LinkedList<Callback>()

    fun addContact(id: String, name: String, system: String,
                   ip: String, broadcastPort: Int, messagePort: Int) {
        addContact(Contact(id, name, system, ip, broadcastPort, messagePort))
    }

    fun addContact(contact: Contact) {
        if (contacts.contains(contact)) {
            return
        }
        contacts.add(contact)
        callbacks.forEach {
            it.onContactAdd(contact)
        }
    }

    fun removeContact(id: String) {
        val index = contacts.indexOfFirst {
            it.id == id
        }
        if (index >= 0 && index < contacts.size) {
            val contact = contacts.removeAt(index)
            callbacks.forEach {
                it.onContactRemove(contact)
            }
        }
    }

    fun broadcastPorts(): List<Int> {
        return contacts.map {
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
        fun onContactRemove(contact: Contact)
    }

}