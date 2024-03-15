package com.github.bytecat.file

import com.github.bytecat.ByteCat
import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.FileRequestData
import com.github.bytecat.protocol.data.FileResponseData
import com.github.bytecat.worker.Worker
import java.util.LinkedList

class FileSendManager(private val myCat: ByteCat) {

    private val pendingSendMap = HashMap<String, SendTask>()

    private val sendList = LinkedList<SendTask>()

    fun newPendingSend(sendTo: Cat, fileReq: FileRequestData, file: IFile) {
        pendingSendMap[fileReq.requestId] = SendTask(sendTo, fileReq, file)
    }

    fun responsePendingSend(fileRes: FileResponseData, worker: Worker) {
        val sendTask = pendingSendMap.remove(fileRes.responseId) ?: return
        if (fileRes.isAccepted) {

            sendList.add(sendTask)

            val sendTo = sendTask.sendTo
            val sender = FileClient(worker)
            sender.start(sendTo.ip, fileRes.streamPort)
            sender.sendFile(sendTask, fileRes.acceptCode)
        }
    }

    data class SendTask (
        val sendTo: Cat,
        val fileReq: FileRequestData,
        val file: IFile,
        val timestamp: Long = System.currentTimeMillis()
    )



}