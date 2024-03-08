package com.github.bytecat.protocol.data

import com.alibaba.fastjson2.JSONObject

interface Data {
    fun toJSONObject(): JSONObject
}