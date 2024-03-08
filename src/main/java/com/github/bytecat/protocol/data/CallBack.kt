package com.github.bytecat.protocol.data

import com.alibaba.fastjson2.JSONObject

class CallBack(val id: String) : Data {

    companion object {
        const val KEY_CALL_BACK_ID = "callBackId"

        fun parse(json: JSONObject): CallBack {
            return CallBack(
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