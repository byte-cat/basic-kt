package com.github.bytecat.file

import com.github.bytecat.contact.Cat

interface TransferCallback {
    fun onStart(owner: Cat, transferId: String, totalSize: Long)
    fun onTransfer(owner: Cat, transferId: String, transferSize: Long, totalSize: Long)
    fun onSuccess(owner: Cat, transferId: String, md5: String)
    fun onError(owner: Cat, transferId: String, e: Throwable)
}