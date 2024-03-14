package com.github.bytecat.ext

import com.github.bytecat.file.IFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

fun IFile.getMD5(): String {
    return openReadStream().getMD5()
}