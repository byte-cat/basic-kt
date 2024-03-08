package com.github.bytecat.protocol.data

import com.alibaba.fastjson2.JSONObject

class Message(val text: String) : Data {

    companion object {
        private const val KEY_TEXT = "text"
        fun parse(json: JSONObject): Message {
            return Message(
                json.getString(KEY_TEXT)
            )
        }
    }

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_TEXT, text)
        }
    }
}