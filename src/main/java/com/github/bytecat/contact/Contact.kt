package com.github.bytecat.contact

data class Contact(
    val ipAddress: String,
    val name: String,
    val deviceType: String,
    val broadcastPort: Int,
    val messagePort: Int
) {
    override fun equals(other: Any?): Boolean {
        return other is Contact && other.ipAddress == ipAddress
    }
}