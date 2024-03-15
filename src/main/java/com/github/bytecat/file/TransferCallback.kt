package com.github.bytecat.file

import com.github.bytecat.contact.Cat
import java.io.File

interface TransferCallback {
    fun onStart(owner: Cat, totalSize: Long)
    fun onTransfer(owner: Cat, transferSize: Long, totalSize: Long)
    fun onEnd(owner: Cat, file: File, md5: String, acceptCode: String)

    fun onError()
}