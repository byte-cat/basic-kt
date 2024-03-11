package com.github.bytecat.protocol

import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.data.CallBackData
import com.github.bytecat.protocol.data.HiData
import com.github.bytecat.protocol.data.HiCallBackData
import com.github.bytecat.protocol.data.MessageData

const val EVENT_HI2A = "hi2a"
const val EVENT_HI2U = "hi2u"
const val EVENT_BYE2A = "bye2a"
const val EVENT_CALL_BACK = "callBack"
const val EVENT_MESSAGE = "msg"




object Protocol {

    fun hiToAll(broadcastPort: Int, messagePort: Int, sysInfo: ISystemInfo): Event {
        return Event(name = EVENT_HI2A, HiData(broadcastPort, messagePort, sysInfo))
    }
    fun hiToYou(broadcastPort: Int, messagePort: Int, sysInfo: ISystemInfo): Event {
        return Event(name = EVENT_HI2U, HiData(broadcastPort, messagePort, sysInfo))
    }
    fun hiToYouCallMeBack(): Event {
        return Event(EVENT_HI2U, HiCallBackData())
    }
    fun byeToAll(): Event {
        return Event(name = EVENT_BYE2A, dataJson = null)
    }
    fun callBack(callBackId: String): Event {
        return Event(EVENT_CALL_BACK, CallBackData(callBackId))
    }

    fun message(text: String): Event {
        return Event(EVENT_MESSAGE, MessageData(text))
    }

}