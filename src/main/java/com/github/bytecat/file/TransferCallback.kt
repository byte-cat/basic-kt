package com.github.bytecat.file

import com.github.bytecat.contact.Cat

interface TransferCallback {
    fun onStart(owner: Cat, totalSize: Long)
    fun onTransfer(owner: Cat, transferSize: Long, totalSize: Long)
    fun onSuccess(owner: Cat, md5: String, acceptCode: String)
    fun onError()
}