package com.github.bytecat.protocol

import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.data.*

const val EVENT_HI2A = "hi2a"
const val EVENT_HI2U = "hi2u"
const val EVENT_BYE2A = "bye2a"
const val EVENT_CALL_BACK = "callBack"
const val EVENT_TEXT = "text"
const val EVENT_FILE_REQUEST = "fileReq"
const val EVENT_FILE_RESPONSE = "fileRes"


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

    fun text(text: String): Event {
        return Event(EVENT_TEXT, TextData(text))
    }

    fun fileRequest(fileReq: FileRequestData): Event {
        return Event(EVENT_FILE_REQUEST, fileReq)
    }

    fun fileResponse(fileRes: FileResponseData): Event {
        return Event(EVENT_FILE_RESPONSE, fileRes)
    }

}