package com.github.bytecat.protocol.data

import org.json.JSONObject

open class TextData protected constructor(text: String) : Data {

    companion object {
        private const val KEY_TEXT = "text"
        fun parse(json: JSONObject): TextData {
            return TextData(
                json.getString(KEY_TEXT)
            )
        }
    }

    var text: String = text
        protected set


    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_TEXT, text)
        }
    }
}