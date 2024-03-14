package com.github.bytecat.protocol.data

import org.json.JSONObject
import java.util.UUID

open class FileResponseData(
    val responseId: String,
    val responseCode: Int,
    val streamPort: Int,
    val acceptCode: String = if (responseCode == RESPONSE_CODE_ACCEPT) {
        UUID.randomUUID().toString()
    } else {
        ""
    }
) : Data {

    companion object {

        private const val KEY_RESPONSE_ID = "responseId"
        private const val KEY_RESPONSE_CODE = "responseCode"
        private const val KEY_STREAM_PORT = "streamPort"
        private const val KEY_ACCEPT_CODE = "acceptCode"

        const val RESPONSE_CODE_REJECT = 0
        const val RESPONSE_CODE_ACCEPT = 1

        fun from(jsonObj: JSONObject): FileResponseData {
            return FileResponseData(
                jsonObj.getString(KEY_RESPONSE_ID),
                jsonObj.getInt(KEY_RESPONSE_CODE),
                jsonObj.getInt(KEY_STREAM_PORT),
                jsonObj.getString(KEY_ACCEPT_CODE)
            )
        }

        fun reject(fileReq: FileRequestData): FileResponseData {
            return FileResponseData(fileReq.requestId, RESPONSE_CODE_REJECT, -1)
        }

        fun accept(fileReq: FileRequestData, streamPort: Int): FileResponseData {
            return FileResponseData(fileReq.requestId, RESPONSE_CODE_ACCEPT, streamPort)
        }

    }

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_RESPONSE_ID, responseId)
            put(KEY_RESPONSE_CODE, responseCode)
            put(KEY_STREAM_PORT, streamPort)
            put(KEY_ACCEPT_CODE, acceptCode)
        }
    }

}