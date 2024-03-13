package com.github.bytecat.protocol.data

import org.json.JSONObject

class CallBackData(val id: String) : Data {

    companion object {
        const val KEY_CALL_BACK_ID = "callBackId"

        fun parse(json: JSONObject): CallBackData {
            return CallBackData(
                json.getString(KEY_CALL_BACK_ID)
            )
        }
    }

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_CALL_BACK_ID, id)
        }
    }
}