package com.github.bytecat.udp.handler

import com.github.bytecat.ByteCat
import com.github.bytecat.protocol.EVENT_BYE2A
import com.github.bytecat.protocol.EVENT_HI2A
import com.github.bytecat.protocol.Event
import com.github.bytecat.protocol.data.HiData

abstract class BroadcastHandler(private val byteCat: ByteCat) : AbsHandler() {
    override fun onReceive(fromIp: String, data: ByteArray) {
        if (fromIp == byteCat.myLocalIp) {
            return
        }
        dispatchReceive(data) { event ->
            when(event.name) {
                EVENT_HI2A -> {
                    val hiData = HiData.parse(event.dataJson!!)
                    onHiToAll(fromIp, event, hiData)
                }
                EVENT_BYE2A -> {
                    onByeToAll(fromIp)
                }
            }
        }
    }

    abstract fun onHiToAll(fromIp: String, event: Event, hiData: HiData)

    abstract fun onByeToAll(fromIp: String)

}