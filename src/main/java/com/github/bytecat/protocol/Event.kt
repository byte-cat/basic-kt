package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject
import java.util.UUID

open class Event(val catId: String, val event: String, val eventId: String = UUID.randomUUID().toString()) {
    open fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_BYTE_CAT_ID, catId)
            put(KEY_EVENT, event)
            put(KEY_EVENT_ID, eventId)
        }
    }
}