package com.github.bytecat.ext

import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

fun InputStream.getMD5(): String {
    val md5 = MessageDigest.getInstance("MD5")
    val buffer = ByteArray(8192)
    var length: Int
    while ((this.read(buffer).also { length = it }) != -1) {
        md5.update(buffer, 0, length)
    }
    this.close()
    val bigInt = BigInteger(1, md5.digest())
    val hashText = bigInt.toString(16)
    return if (hashText.length < 32) {
        val sb = StringBuilder(hashText)
        val pre = CharArray(32 - hashText.length) {
            '0'
        }
        sb.insert(0, pre).toString()
    } else {
        hashText
    }
}