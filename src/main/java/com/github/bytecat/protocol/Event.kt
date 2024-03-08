package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.protocol.data.Data
import java.util.UUID

class Event(
    val name: String,
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val dataJson: JSONObject? = null
) {

    companion object {

        private const val KEY_EVENT = "event"
        private const val KEY_EVENT_ID = "eventId"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_DATA = "data"
    }

    constructor(name: String, data: Data): this(name, dataJson = data.toJSONObject())

    constructor(jsonStr: String): this(JSONObject.parseObject(jsonStr))

    constructor(json: JSONObject): this(
        json.getString(KEY_EVENT),
        json.getString(KEY_EVENT_ID),
        json.getLong(KEY_TIMESTAMP),
        if (json.containsKey(KEY_DATA)) {
            json.getJSONObject(KEY_DATA)
        } else {
            null
        }
    )

    fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_EVENT, name)
            put(KEY_EVENT_ID, id)
            put(KEY_TIMESTAMP, timestamp)
            if (dataJson != null) {
                put(KEY_DATA, dataJson)
            }
        }
    }
}