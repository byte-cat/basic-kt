package com.github.bytecat.protocol.data

import com.github.bytecat.ext.getMD5
import com.github.bytecat.file.IFile
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

open class FileRequestData protected constructor(requestId: String = UUID.randomUUID().toString(), name: String, size: Long, md5: String) : Data {

    companion object {
        private const val KEY_REQUEST_ID = "requestId"
        private const val KEY_NAME = "name"
        private const val KEY_SIZE = "size"
        private const val KEY_MD5 = "md5"

        fun from(json: JSONObject): FileRequestData {
            return FileRequestData(
                json.getString(KEY_REQUEST_ID),
                json.getString(KEY_NAME),
                json.getLong(KEY_SIZE),
                json.getString(KEY_MD5)
            )
        }

        fun from(file: IFile): FileRequestData {
            return FileRequestData(name = file.getName(), size = file.length(), md5 = file.getMD5())
        }

        fun from(fileName: String, inputStream: InputStream): FileRequestData {
            val size = inputStream.available()
            val md5 = inputStream.getMD5()
            return FileRequestData(name = fileName, size = size.toLong(), md5 = md5)
        }

    }

    var requestId: String = requestId
        protected set
    var name: String = name
        protected set
    var size: Long = size
        protected set
    var md5: String = md5
        protected set


    fun accept(streamPort: Int): FileResponseData {
        return FileResponseData.accept(this, streamPort)
    }

    fun reject(): FileResponseData {
        return FileResponseData.reject(this)
    }

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_REQUEST_ID, requestId)
            put(KEY_NAME, name)
            put(KEY_SIZE, size)
            put(KEY_MD5, md5)
        }
    }
}