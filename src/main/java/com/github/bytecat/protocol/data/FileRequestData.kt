package com.github.bytecat.protocol.data

import com.github.bytecat.ext.getMD5
import org.json.JSONObject
import java.io.File
import java.util.UUID

class FileRequestData private constructor (
    val requestId: String = UUID.randomUUID().toString(),
    val name: String,
    val size: Long,
    val md5: String
) : Data {

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

        fun from(file: File): FileRequestData {
            return FileRequestData(name = file.name, size = file.length(), md5 = file.getMD5())
        }

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