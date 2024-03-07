package com.github.bytecat.contact

data class Contact(
    val id: String,
    val name: String,
    val deviceType: String,
    val ipAddress: String,
    val broadcastPort: Int,
    val messagePort: Int
) {
    override fun equals(other: Any?): Boolean {
        return other is Contact && other.id == id
    }
}