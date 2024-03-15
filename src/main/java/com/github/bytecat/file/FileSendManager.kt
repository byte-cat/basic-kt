package com.github.bytecat.file

import com.github.bytecat.ByteCat
import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.FileRequestData
import com.github.bytecat.protocol.data.FileResponseData
import com.github.bytecat.worker.Worker
import java.io.File
import java.util.LinkedList

class FileSendManager(private val myCat: ByteCat) {

    private val pendingSendMap = HashMap<String, SendTask>()

    private val sendList = LinkedList<SendTask>()

    private val outerCallbacks = LinkedList<TransferCallback>()
    private val transferCallback = object : TransferCallback {
        override fun onStart(owner: Cat, totalSize: Long) {
            outerCallbacks.forEach {
                it.onStart(owner, totalSize)
            }
        }

        override fun onTransfer(owner: Cat, transferSize: Long, totalSize: Long) {
            outerCallbacks.forEach {
                it.onTransfer(owner, transferSize, totalSize)
            }
        }

        override fun onSuccess(owner: Cat, md5: String, acceptCode: String) {
            outerCallbacks.forEach {
                it.onSuccess(owner, md5, acceptCode)
            }
        }

        override fun onError() {
            outerCallbacks.forEach {
                it.onError()
            }
        }
    }

    fun newPendingSend(sendTo: Cat, fileReq: FileRequestData, file: IFile) {
        pendingSendMap[fileReq.requestId] = SendTask(sendTo, fileReq, file)
    }

    fun responsePendingSend(fileRes: FileResponseData, worker: Worker) {
        val sendTask = pendingSendMap.remove(fileRes.responseId) ?: return
        if (fileRes.isAccepted) {

            sendList.add(sendTask)

            val sendTo = sendTask.sendTo
            val sender = FileClient(worker)
            sender.callback = transferCallback
            sender.start(sendTo.ip, fileRes.streamPort)
            sender.sendFile(sendTask, fileRes.acceptCode)
        }
    }

    fun registerCallback(callback: TransferCallback) {
        if (outerCallbacks.contains(callback)) {
            return
        }
        outerCallbacks.add(callback)
    }
    fun unregisterCallback(callback: TransferCallback) {
        outerCallbacks.remove(callback)
    }

    data class SendTask (
        val sendTo: Cat,
        val fileReq: FileRequestData,
        val file: IFile,
        val timestamp: Long = System.currentTimeMillis()
    )

}