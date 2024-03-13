package com.github.bytecat

import com.github.bytecat.contact.Cat
import com.github.bytecat.contact.CatBook
import com.github.bytecat.handler.IHandler
import com.github.bytecat.handler.SimpleHandler
import com.github.bytecat.message.MessageBox
import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.*
import com.github.bytecat.protocol.data.*
import com.github.bytecat.udp.UDPReceiver
import com.github.bytecat.utils.FileRequestObserver
import com.github.bytecat.utils.IDebugger
import com.github.bytecat.utils.getLocalIP
import com.github.bytecat.worker.Worker
import org.json.JSONObject
import java.io.File
import java.io.InputStream

open class ByteCat {

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"

        private val BROADCAST_PREPARE_PORTS = arrayOf(1123, 5813, 2134, 5589, 3141)
        private val MESSAGE_PREPARE_PORTS = arrayOf(3211, 3185, 4312, 9855, 1413)
    }

    private val myLocalIp = getLocalIP()

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
            if (fromIp == myLocalIp) {
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
                            handler.cancel(refreshTask);
                        }
                    }
                    EVENT_MESSAGE -> {
                        val msg = TextData.parse(event.dataJson!!)
                        catBook.findCatByIp(fromIp)?.run {
                            MessageBox.obtain(this).onTextReceived(msg)
                        }
                    }
                    EVENT_FILE_REQUEST -> {
                        val fileReqData = FileRequestData.from(event.dataJson!!)
                        catBook.findCatByIp(fromIp)?.run {
                            MessageBox.obtain(this).onFileRequestReceived(fileReqData)
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

    private val refreshTask = Runnable {
        if (refreshingCats.isNotEmpty()) {
            for ((_, cat) in refreshingCats) {
                catBook.removeCat(cat.ip)
            }
            refreshingCats.clear()
        }
    }
    private val refreshingCats = HashMap<String, Cat>()

    private var catCallback: Callback? = null

    private val worker = Worker()

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
            handler.post(delay = 1000L, refreshTask)
        }
    }

    fun sendMessage(cat: Cat, text: String) {
        handler.post {
            udpSender.sendMessage(cat.ip, cat.messagePort, text)
        }
    }

    fun sendFileRequest(cat: Cat, file: File, observer: FileRequestObserver? = null) {
        observer?.run {
            handler.post {
                onParseStart()
            }
        }

        worker.queueWork({ Protocol.fileRequest(file) }) {
            observer?.run {
                handler.post {
                    onParseEnd()
                }
            }
            handler.post {
                udpSender.send(cat.ip, cat.messagePort, it)
            }
        }
    }

    fun sendFileRequest(cat: Cat, fileName: String, inStream: InputStream, observer: FileRequestObserver? = null) {
        observer?.run {
            handler.post {
                onParseStart()
            }
        }

        worker.queueWork({ Protocol.fileRequest(fileName, inStream) }) {
            observer?.run {
                handler.post {
                    onParseEnd()
                }
            }
            handler.post {
                udpSender.send(cat.ip, cat.messagePort, it)
            }
        }
    }

    fun rejectFileRequest(cat: Cat, fileReq: FileRequestData) {
        handler.post {
            udpSender.send(cat.ip, messageReceiver.port, Protocol.fileResponseReject(fileReq).toJSONObject())
        }
    }

    fun acceptFileRequest(cat: Cat, fileReq: FileRequestData) {
        handler.post {
            // TODO
             println("acceptFileRequest --->")
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

            broadcastReceiver.close()
            messageReceiver.close()
            udpSender.close()

            handler.shutdown()
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
            catCallback?.onReady(Cat(myLocalIp, systemInfo.systemUserName, systemInfo.system,
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
        val jsonObj = JSONObject(text)
        handler.invoke(Event(jsonObj))
    }

    interface Callback {
        fun onReady(myCat: Cat)
    }

}