package com.github.bytecat.protocol.data

import com.alibaba.fastjson2.JSONObject

class HiCallBackData(val callMeBack: Boolean = true) : Data {

    companion object {
        private const val KEY_CALL_ME_BACK = "callMeBack"

        fun parse(json: JSONObject): HiCallBackData {
            return HiCallBackData(json.getBooleanValue(KEY_CALL_ME_BACK, false))
        }
    }

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_CALL_ME_BACK, callMeBack)
        }
    }
}