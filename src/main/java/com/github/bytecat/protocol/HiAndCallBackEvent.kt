package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject

class HiAndCallBackEvent(catId: String) : Event(catId, EVENT_HI2U) {
    override fun toJSONObject(): JSONObject {
        return super.toJSONObject().apply {
            put(KEY_CALL_ME_BACK, true)
        }
    }
}