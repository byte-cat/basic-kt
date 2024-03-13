package com.github.bytecat.ext

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

fun File.getMD5(): String {
    return FileInputStream(this).getMD5()
}