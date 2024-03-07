package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject
import java.util.UUID

open class Event(val event: String, val eventId: String = UUID.randomUUID().toString()) {
    open fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_EVENT, event)
            put(KEY_EVENT_ID, eventId)
        }
    }
}