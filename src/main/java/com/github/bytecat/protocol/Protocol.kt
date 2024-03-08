package com.github.bytecat.protocol

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.data.CallBack
import com.github.bytecat.protocol.data.Hi
import com.github.bytecat.protocol.data.HiCallBack
import com.github.bytecat.protocol.data.Message

const val EVENT_HI2A = "hi2a"
const val EVENT_HI2U = "hi2u"
const val EVENT_BYE2A = "bye2a"
const val EVENT_CALL_BACK = "callBack"
const val EVENT_MESSAGE = "msg"




object Protocol {

    fun hiToAll(broadcastPort: Int, messagePort: Int, sysInfo: ISystemInfo): Event {
        return Event(name = EVENT_HI2A, Hi(broadcastPort, messagePort, sysInfo))
    }
    fun hiToYou(broadcastPort: Int, messagePort: Int, sysInfo: ISystemInfo): Event {
        return Event(name = EVENT_HI2U, Hi(broadcastPort, messagePort, sysInfo))
    }
    fun hiToYouCallMeBack(): Event {
        return Event(EVENT_HI2U, HiCallBack())
    }
    fun byeToAll(): Event {
        return Event(EVENT_BYE2A)
    }
    fun callBack(callBackId: String): Event {
        return Event(EVENT_CALL_BACK, CallBack(callBackId))
    }

    fun message(text: String): Event {
        return Event(EVENT_MESSAGE, Message(text))
    }

}