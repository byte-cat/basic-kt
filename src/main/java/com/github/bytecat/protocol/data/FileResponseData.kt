package com.github.bytecat.protocol.data

import org.json.JSONObject

class FileResponseData private constructor (
    val responseId: String,
    val responseCode: Int,
    val streamPort: Int
) : Data {

    companion object {

        private const val KEY_RESPONSE_ID = "responseId"
        private const val KEY_RESPONSE_CODE = "responseCode"
        private const val KEY_STREAM_PORT = "streamPort"

        const val RESPONSE_CODE_REJECT = 0
        const val RESPONSE_CODE_ACCEPT = 1

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
        }
    }

}