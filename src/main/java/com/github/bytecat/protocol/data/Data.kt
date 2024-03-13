package com.github.bytecat.protocol.data

import org.json.JSONObject

interface Data {
    fun toJSONObject(): JSONObject
}