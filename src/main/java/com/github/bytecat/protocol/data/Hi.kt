package com.github.bytecat.protocol.data

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.platform.ISystemInfo

class Hi(
    val broadcastPort: Int, val messagePort: Int,
    val systemUserName: String, val osName: String
) : Data {

    companion object {
        const val KEY_BROADCAST_PORT = "broadcastPort"
        const val KEY_MESSAGE_PORT = "messagePort"
        const val KEY_SYS_USER_NAME = "sysUserName"
        const val KEY_OS_NAME = "osName"

        fun parse(json: JSONObject): Hi {
            return Hi(
                json.getIntValue(KEY_BROADCAST_PORT),
                json.getIntValue(KEY_MESSAGE_PORT),
                json.getString(KEY_SYS_USER_NAME),
                json.getString(KEY_OS_NAME),
            )
        }
    }

    constructor(broadcastPort: Int, messagePort: Int, sysInfo: ISystemInfo): this(broadcastPort, messagePort, sysInfo.systemUserName, sysInfo.system)

    override fun toJSONObject(): JSONObject {
        return JSONObject().apply {
            put(KEY_BROADCAST_PORT, broadcastPort)
            put(KEY_MESSAGE_PORT, messagePort)
            put(KEY_SYS_USER_NAME, systemUserName)
            put(KEY_OS_NAME, osName)
        }
    }
}