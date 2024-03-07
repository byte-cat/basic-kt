package com.github.bytecat

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.contact.Contact
import com.github.bytecat.contact.CatBook
import com.github.bytecat.handler.IHandler
import com.github.bytecat.handler.SimpleHandler
import com.github.bytecat.platform.IPlatform
import com.github.bytecat.protocol.*
import com.github.bytecat.utils.getLocalIP
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

open class ByteCat {

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"

        private val BROADCAST_PREPARE_PORTS = arrayOf(1123, 5813, 2134, 5589, 3141)
        private val MESSAGE_PREPARE_PORTS = arrayOf(3211, 3185, 4312, 9855, 1413)
    }

    private val myLocalIP = getLocalIP()

    open val debugger: IDebugger? = null

    open val platform: IPlatform by lazy {
        object : IPlatform {
            override val systemUserName: String = System.getenv()["USER"] ?: "Unknown"
            override val system: String = System.getProperty("os.name")
        }
    }

    private val protocol by lazy { Protocol(platform) }

    private val broadcastListener = object : UDPReceiver.OnReceiveListener {
        override fun onReady() {
            debugger?.onBroadcastReady()
            trySayHiToAll()
        }

        override fun onReceive(fromIp: String, data: ByteArray) {
            debugger?.onBroadcastReceived(fromIp, data)
            if (fromIp == myLocalIP) {
                return
            }
            dispatchReceive(data) { event, jsonObj ->
                when(event) {
                    EVENT_HI2A -> {

                        val sysUserName = jsonObj.getString(KEY_SYS_USER_NAME)
                        val osName = jsonObj.getString(KEY_OS_NAME)

                        val broadcastPort = jsonObj.getIntValue(KEY_BROADCAST_PORT)
                        val messagePort = jsonObj.getIntValue(KEY_MESSAGE_PORT)

                        catBook.addContact(sysUserName, osName, fromIp, broadcastPort, messagePort)

                        udpSender.send(fromIp, messagePort, protocol.hi2You(
                            broadcastReceiver.port,
                            messageReceiver.port
                        ))
                    }
                    EVENT_BYE2A -> {
                        catBook.removeContact(fromIp)
                    }
                }
            }
        }
    }

    private val messageListener = object : UDPReceiver.OnReceiveListener {
        override fun onReady() {
            debugger?.onMessageReady()
            trySayHiToAll()
        }

        override fun onReceive(fromIp: String, data: ByteArray) {
            debugger?.onMessageReceived(fromIp, data)
            dispatchReceive(data) {event, jsonObj ->
                when(event) {
                    EVENT_HI2U -> {
                        val callMeBack = jsonObj.getBooleanValue(KEY_CALL_ME_BACK, false)

                        if (callMeBack) {
                            val cat = catBook.findByIP(fromIp)
                            if (cat != null) {
                                val eventId = jsonObj.getString(KEY_EVENT_ID)
                                udpSender.send(fromIp, cat.messagePort, protocol.callBack(eventId))
                            }
                        } else {
                            val sysUserName = jsonObj.getString(KEY_SYS_USER_NAME)
                            val osName = jsonObj.getString(KEY_OS_NAME)

                            val broadcastPort = jsonObj.getIntValue(KEY_BROADCAST_PORT)
                            val messagePort = jsonObj.getIntValue(KEY_MESSAGE_PORT)

                            catBook.addContact(sysUserName, osName, fromIp, broadcastPort, messagePort)
                        }
                    }
                    EVENT_CALL_BACK -> {
                        val callBackId = jsonObj.getString(KEY_CALL_BACK_ID)
                        refreshingCats.remove(callBackId)
                        if (refreshingCats.isEmpty()) {
                            refreshTimer.cancel()
                        }
                    }
                }
            }
        }
    }

    private lateinit var broadcastReceiver: UDPReceiver
    private lateinit var messageReceiver: UDPReceiver

    open val handler: IHandler by lazy { SimpleHandler().also { it.start() } }

    private val udpSender by lazy { UDPSender() }

    @Volatile
    private var isReady = false

    private val contactCallback = object : CatBook.Callback {

        override fun onContactAdd(contact: Contact) {
            debugger?.onContactAdd(contact)
        }

        override fun onContactUpdate(contact: Contact) {

        }

        override fun onContactRemove(contact: Contact) {
            debugger?.onContactRemove(contact)
        }
    }

    val catBook = CatBook()

    private val refreshingCats = HashMap<String, Contact>()

    private val refreshTimer = Timer()

    fun startup() {
        for (port in BROADCAST_PREPARE_PORTS) {
            val receiver = UDPReceiver(port)
            if (receiver.listen(broadcastListener)) {
                if (!::broadcastReceiver.isInitialized) {
                    broadcastReceiver = receiver
                    break
                }
            }
        }
        for (port in MESSAGE_PREPARE_PORTS) {
            val receiver = UDPReceiver(port)
            if (receiver.listen(messageListener)) {
                if (!::messageReceiver.isInitialized) {
                    messageReceiver = receiver
                    break
                }
            }
        }
        if (!::broadcastReceiver.isInitialized || !::messageReceiver.isInitialized) {
            throw IllegalStateException("No legal ports available.")
        }
    }

    fun refresh() {
        if (catBook.cats.isEmpty()) {
            return
        }
        handler.post {
            catBook.cats.forEach {
                val event = protocol.hi2YouAndCallback()
                udpSender.send(it.ipAddress, it.messagePort, event.toJSONObject().toString())

                refreshingCats[event.eventId] = it
            }
            refreshTimer.schedule(object : TimerTask() {
                override fun run() {
                    if (refreshingCats.isNotEmpty()) {
                        for ((_, cat) in refreshingCats) {
                            catBook.removeContact(cat.ipAddress)
                        }
                        refreshingCats.clear()
                    }
                }
            }, 1000L)
        }
    }

    fun shutdown() {
        handler.post {
            for (port in BROADCAST_PREPARE_PORTS) {
                udpSender.send(BROADCAST_IP, port, protocol.bye2All())
            }
            catBook.unregisterCallback(contactCallback)

            refreshTimer.cancel()

            broadcastReceiver.close()
            messageReceiver.close()
            udpSender.close()
        }
    }

    @Synchronized
    private fun trySayHiToAll() {
        if (isReady) {
            return
        }
        if (!::broadcastReceiver.isInitialized || !::messageReceiver.isInitialized) {
            return
        }
        if (!broadcastReceiver.isReady || !messageReceiver.isReady) {
            return
        }
        handler.post {
            catBook.registerCallback(contactCallback)
            for (port in BROADCAST_PREPARE_PORTS) {
                udpSender.send(
                    BROADCAST_IP, port,
                    protocol.hi2All(broadcastReceiver.port, messageReceiver.port)
                )
            }
        }

        isReady = true
    }

    private fun dispatchReceive(data: ByteArray, handler: (event: String, jsonObj: JSONObject) -> Unit) {
        val text = String(data)
        val jsonObj = JSONObject.parseObject(text)
        val event = jsonObj.getString(KEY_EVENT)
        handler.invoke(event, jsonObj)
    }

}