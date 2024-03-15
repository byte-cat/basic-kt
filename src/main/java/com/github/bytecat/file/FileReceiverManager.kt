package com.github.bytecat.file

import com.github.bytecat.ByteCat
import com.github.bytecat.contact.Cat
import com.github.bytecat.protocol.data.FileRequestData
import com.github.bytecat.protocol.data.FileResponseData

class FileReceiverManager(private val myCat: ByteCat) {

    private val fileInfoMap = HashMap<String, FileInfo>()

    fun accept(fileReq: FileRequestData, cat: Cat, streamPort: Int): FileResponseData {
        val acceptRes = fileReq.accept(streamPort)
        fileInfoMap[acceptRes.acceptCode] = FileInfo(cat, fileReq.name, fileReq.size, fileReq.md5)

        return acceptRes
    }

    fun getFileInfo(acceptCode: String): FileInfo? = fileInfoMap[acceptCode]

    fun removeFileInfo(acceptCode: String) {
        fileInfoMap.remove(acceptCode)
    }

    data class FileInfo(val receiveFrom: Cat, val name: String, val length: Long, val md5: String)
}