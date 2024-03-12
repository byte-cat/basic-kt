package com.github.bytecat

import com.alibaba.fastjson2.JSONObject
import com.github.bytecat.contact.Cat
import com.github.bytecat.contact.CatBook
import com.github.bytecat.handler.IHandler
import com.github.bytecat.handler.SimpleHandler
import com.github.bytecat.message.MessageBox
import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.*
import com.github.bytecat.protocol.data.CallBackData
import com.github.bytecat.protocol.data.HiData
import com.github.bytecat.protocol.data.HiCallBackData
import com.github.bytecat.protocol.data.TextData
import com.github.bytecat.udp.UDPReceiver
import com.github.bytecat.utils.IDebugger
import com.github.bytecat.utils.getLocalIP
import java.util.Timer
import java.util.TimerTask

open class ByteCat {

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"

        private val BROADCAST_PREPARE_PORTS = arrayOf(1123, 5813, 2134, 5589, 3141)
        private val MESSAGE_PREPARE_PORTS = arrayOf(3211, 3185, 4312, 9855, 1413)
    }

    private val myLocalIP = getLocalIP()

    open val debugger: IDebugger? = null

    open val systemInfo: ISystemInfo by lazy {
        object : ISystemInfo {
            override val systemUserName: String = System.getenv()["USER"] ?: "Unknown"
            override val system: String = System.getProperty("os.name")
        }
    }

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
            dispatchReceive(data) { event ->
                when(event.name) {
                    EVENT_HI2A -> {
                        val hiData = HiData.parse(event.dataJson!!)

                        catBook.addCat(hiData.systemUserName, hiData.osName, fromIp, hiData.broadcastPort, hiData.messagePort)

                        udpSender.send(fromIp, hiData.messagePort, Protocol.hiToYou(
                            broadcastReceiver.port,
                            messageReceiver.port,
                            systemInfo
                        ))
                    }
                    EVENT_BYE2A -> {
                        catBook.removeCat(fromIp)
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
            dispatchReceive(data) { event ->
                when(event.name) {
                    EVENT_HI2U -> {
                        val callBack = HiCallBackData.parse(event.dataJson!!)
                        if (callBack.callMeBack) {
                            val cat = catBook.findCatByIp(fromIp)
                            if (cat != null) {
                                udpSender.send(fromIp, cat.messagePort, Protocol.callBack(event.id))
                            }
                        } else {
                            val hiData = HiData.parse(event.dataJson)
                            catBook.addCat(hiData.systemUserName, hiData.osName, fromIp, hiData.broadcastPort, hiData.messagePort)
                        }
                    }
                    EVENT_CALL_BACK -> {
                        val callBackData = CallBackData.parse(event.dataJson!!)
                        refreshingCats.remove(callBackData.id)
                        if (refreshingCats.isEmpty()) {
                            refreshTimer?.cancel()
                            refreshTimer = null
                        }
                    }
                    EVENT_MESSAGE -> {
                        val msg = TextData.parse(event.dataJson!!)
                        catBook.findCatByIp(fromIp)?.run {
                            MessageBox.obtain(this).onMessageReceived(msg)
                        }
                    }
                }
            }
        }
    }

    private lateinit var broadcastReceiver: UDPReceiver
    private lateinit var messageReceiver: UDPReceiver

    open val handler: IHandler by lazy { SimpleHandler() }

    private val udpSender by lazy { CatSender() }

    @Volatile
    private var isReady = false

    private val contactCallback = object : CatBook.Callback {

        override fun onContactAdd(cat: Cat) {
            debugger?.onContactAdd(cat)
        }

        override fun onContactUpdate(cat: Cat) {

        }

        override fun onContactRemove(cat: Cat) {
            debugger?.onContactRemove(cat)
        }
    }

    val catBook = CatBook()

    private val refreshingCats = HashMap<String, Cat>()

    private var refreshTimer: Timer? = null

    private var catCallback: Callback? = null

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
                val event = Protocol.hiToYouCallMeBack()
                udpSender.send(it.ip, it.messagePort, event.toJSONObject().toString())

                refreshingCats[event.id] = it
            }
            if (refreshTimer != null) {
                refreshTimer?.cancel()
                refreshTimer = null
            }
            refreshTimer = Timer()
            refreshTimer?.schedule(object : TimerTask() {
                override fun run() {
                    if (refreshingCats.isNotEmpty()) {
                        for ((_, cat) in refreshingCats) {
                            catBook.removeCat(cat.ip)
                        }
                        refreshingCats.clear()
                    }
                }
            }, 1000L)
        }
    }

    fun sendMessage(cat: Cat, text: String) {
        handler.post{
            udpSender.sendMessage(cat.ip, cat.messagePort, text)
        }
    }

    fun setCallback(callback: Callback) {
        this.catCallback = callback
    }

    fun shutdown() {
        handler.post {
            for (port in BROADCAST_PREPARE_PORTS) {
                udpSender.send(BROADCAST_IP, port, Protocol.byeToAll())
            }

            catCallback = null
            catBook.unregisterCallback(contactCallback)

            refreshTimer?.cancel()
            refreshTimer = null

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
            catCallback?.onReady(Cat(myLocalIP, systemInfo.systemUserName, systemInfo.system,
                broadcastReceiver.port, messageReceiver.port))
            catBook.registerCallback(contactCallback)
            for (port in BROADCAST_PREPARE_PORTS) {
                udpSender.send(
                    BROADCAST_IP, port,
                    Protocol.hiToAll(broadcastReceiver.port, messageReceiver.port, systemInfo)
                )
            }
        }

        isReady = true
    }

    private fun dispatchReceive(data: ByteArray, handler: (event: Event) -> Unit) {
        val text = String(data)
        val jsonObj = JSONObject.parseObject(text)
        handler.invoke(Event(jsonObj))
    }

    interface Callback {
        fun onReady(myCat: Cat)
    }

}