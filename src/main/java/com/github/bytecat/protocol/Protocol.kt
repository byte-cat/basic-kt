package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.platform.IPlatform
import java.util.UUID

const val EVENT_HI2A = "hi2a"
const val EVENT_HI2U = "hi2u"
const val EVENT_BYE2A = "bye2a"
const val EVENT_CALL_BACK = "callBack"

const val KEY_EVENT = "event"
const val KEY_EVENT_ID = "eventId"
const val KEY_BYTE_CAT_ID = "byteCatId"
const val KEY_BROADCAST_PORT = "broadcastPort"
const val KEY_MESSAGE_PORT = "messagePort"
const val KEY_SYS_USER_NAME = "sysUserName"
const val KEY_OS_NAME = "osName"
const val KEY_CALL_ME_BACK = "callMeBack"
const val KEY_CALL_BACK_ID = "callBackId"

class Protocol(private val platform: IPlatform) {
    fun hi2All(catId: String, broadcastPort: Int, messagePort: Int): String {

        return JSONObject().apply {
            put(KEY_EVENT, EVENT_HI2A)
            put(KEY_EVENT_ID, UUID.randomUUID().toString())
            put(KEY_BYTE_CAT_ID, catId)
            put(KEY_BROADCAST_PORT, broadcastPort)
            put(KEY_MESSAGE_PORT, messagePort)
            put(KEY_SYS_USER_NAME, platform.systemUserName)
            put(KEY_OS_NAME, platform.system)
        }.toString()
    }

    fun bye2All(catId: String): String {
        return JSONObject().apply {
            put(KEY_EVENT, EVENT_BYE2A)
            put(KEY_EVENT_ID, UUID.randomUUID().toString())
            put(KEY_BYTE_CAT_ID, catId)
        }.toString()
    }

    fun hi2You(catId: String, broadcastPort: Int, messagePort: Int): String {
        return JSONObject().apply {
            put(KEY_EVENT, EVENT_HI2U)
            put(KEY_EVENT_ID, UUID.randomUUID().toString())
            put(KEY_BYTE_CAT_ID, catId)
            put(KEY_BROADCAST_PORT, broadcastPort)
            put(KEY_MESSAGE_PORT, messagePort)
            put(KEY_SYS_USER_NAME, platform.systemUserName)
            put(KEY_OS_NAME, platform.system)
        }.toString()
    }

    fun hi2YouAndCallback(catId: String): HiAndCallBackEvent {
        return HiAndCallBackEvent(catId)
    }

    fun callBack(catId: String, callBackId: String): String {
        return JSONObject().apply {
            put(KEY_EVENT, EVENT_CALL_BACK)
            put(KEY_BYTE_CAT_ID, catId)
            put(KEY_CALL_BACK_ID, callBackId)
        }.toString()
    }

}