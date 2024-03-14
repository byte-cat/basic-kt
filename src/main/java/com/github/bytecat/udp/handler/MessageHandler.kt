package com.github.bytecat.udp.handler

import com.github.bytecat.protocol.*
import com.github.bytecat.protocol.data.*

abstract class MessageHandler : AbsHandler() {
    override fun onReceive(fromIp: String, data: ByteArray) {
        dispatchReceive(data) { event ->
            when(event.name) {
                EVENT_HI2U -> {
                    val callBack = HiCallBackData.parse(event.dataJson!!)
                    if (callBack.callMeBack) {
                        onHiToYouAndCallMeBack(fromIp, event)
                    } else {
                        val hiData = HiData.parse(event.dataJson)
                        onHiToYou(fromIp, event, hiData)
                    }
                }
                EVENT_CALL_BACK -> {
                    val callBackData = CallBackData.parse(event.dataJson!!)
                    onCallBack(fromIp, event, callBackData)
                }
                EVENT_TEXT -> {
                    val text = TextData.parse(event.dataJson!!)
                    onText(fromIp, text)
                }
                EVENT_FILE_REQUEST -> {
                    val fileReqData = FileRequestData.from(event.dataJson!!)
                    onFileRequest(fromIp, fileReqData)
                }
                EVENT_FILE_RESPONSE -> {
                    val fileResData = FileResponseData.from(event.dataJson!!)
                    onFileResponse(fromIp, fileResData)
                }
            }
        }
    }

    abstract fun onHiToYou(fromIp: String, event: Event, hiData: HiData)
    abstract fun onHiToYouAndCallMeBack(fromIp: String, event: Event)
    abstract fun onCallBack(fromIp: String, event: Event, callBackData: CallBackData)
    abstract fun onText(fromIp: String, text: TextData)
    abstract fun onFileRequest(fromIp: String, fileReqData: FileRequestData)
    abstract fun onFileResponse(fromIp: String, fileResData: FileResponseData)

}