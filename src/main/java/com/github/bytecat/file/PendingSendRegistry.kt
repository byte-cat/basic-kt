package com.github.bytecat.file

import com.github.bytecat.protocol.data.FileRequestData
import com.github.bytecat.protocol.data.FileResponseData

class PendingSendRegistry {

    private val map = HashMap<String, PendingSend>()

    fun request(fileReq: FileRequestData, file: IFile) {
        map[fileReq.requestId] = PendingSend(file, fileReq.md5)
    }

    fun response(fileRes: FileResponseData): PendingSend? {
        return map.remove(fileRes.responseId)
    }


    data class PendingSend(val file: IFile, val md5: String)

}