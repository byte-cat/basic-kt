package com.github.bytecat.protocol.data

import org.json.JSONObject

open class TextData(val text: String) : Data {

    companion object {
        private const val KEY_TEXT = "text"
        fun parse(json: JSONObject): TextData {
            return TextData(
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