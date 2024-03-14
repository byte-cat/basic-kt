package com.github.bytecat

import com.github.bytecat.contact.Cat
import com.github.bytecat.contact.CatBook
import com.github.bytecat.file.FileClient
import com.github.bytecat.handler.IHandler
import com.github.bytecat.handler.SimpleHandler
import com.github.bytecat.message.MessageBox
import com.github.bytecat.platform.ISystemInfo
import com.github.bytecat.protocol.*
import com.github.bytecat.protocol.data.*
import com.github.bytecat.file.FileServer
import com.github.bytecat.file.IFile
import com.github.bytecat.file.PendingSendRegistry
import com.github.bytecat.udp.UDPReceiver
import com.github.bytecat.udp.handler.BroadcastHandler
import com.github.bytecat.udp.handler.MessageHandler
import com.github.bytecat.utils.FileRequestObserver
import com.github.bytecat.utils.IDebugger
import com.github.bytecat.utils.getLocalIP
import com.github.bytecat.worker.Worker
import java.io.File

open class ByteCat {

    companion object {
        private const val BROADCAST_IP = "255.255.255.255"

        private val BROADCAST_PREPARE_PORTS = arrayOf(1123, 5813, 2134, 5589, 3141)
        private val MESSAGE_PREPARE_PORTS = arrayOf(3211, 3185, 4312, 9855, 1413)
    }

    val myLocalIp = getLocalIP()

    open val debugger: IDebugger? = null

    open val systemInfo: ISystemInfo by lazy {
        object : ISystemInfo {
            override val systemUserName: String = System.getenv()["USER"] ?: "Unknown"
            override val system: String = System.getProperty("os.name")
        }
    }

    open val handler: IHandler by lazy { SimpleHandler() }

    open var outputDir = File("./download/").also {
        if (!it.exists()) {
            it.mkdirs()
        }
    }

    val catBook = CatBook()

    private val broadcastHandler by lazy {
        object : BroadcastHandler(this) {
            override fun onHiToAll(fromIp: String, event: Event, hiData: HiData) {
                catBook.addCat(hiData.systemUserName, hiData.osName, fromIp, hiData.broadcastPort, hiData.messagePort)

                udpSender.send(fromIp, hiData.messagePort, Protocol.hiToYou(
                    broadcastReceiver.port,
                    messageReceiver.port,
                    systemInfo
                ))
            }

            override fun onByeToAll(fromIp: String) {
                catBook.removeCat(fromIp)
            }

            override fun onReady() {
                debugger?.onBroadcastReady()
                trySayHiToAll()
            }

        }
    }

    private val messageHandler = object : MessageHandler(){
        override fun onReady() {
            debugger?.onMessageReady()
            trySayHiToAll()
        }

        override fun onHiToYou(fromIp: String, event: Event, hiData: HiData) {
            catBook.addCat(hiData.systemUserName, hiData.osName, fromIp, hiData.broadcastPort, hiData.messagePort)
        }

        override fun onHiToYouAndCallMeBack(fromIp: String, event: Event) {
            val cat = catBook.findCatByIp(fromIp)
            if (cat != null) {
                udpSender.send(fromIp, cat.messagePort, Protocol.callBack(event.id))
            }
        }

        override fun onCallBack(fromIp: String, event: Event, callBackData: CallBackData) {
            refreshingCats.remove(callBackData.id)
            if (refreshingCats.isEmpty()) {
                handler.cancel(refreshTask);
            }
        }

        override fun onText(fromIp: String, text: TextData) {
            catBook.findCatByIp(fromIp)?.run {
                MessageBox.obtain(this).onTextReceived(text)
            }
        }

        override fun onFileRequest(fromIp: String, fileReqData: FileRequestData) {
            catBook.findCatByIp(fromIp)?.run {
                MessageBox.obtain(this).onFileRequestReceived(fileReqData)
            }
        }

        override fun onFileResponse(fromIp: String, fileResData: FileResponseData) {
            val pendingSend = pendingSendRegistry.response(fileResData)!!
            when (fileResData.responseCode) {
                FileResponseData.RESPONSE_CODE_ACCEPT -> {
                    FileClient(worker).run {
                        start(fromIp, fileResData.streamPort)
                        sendFile(pendingSend.file, fileResData.acceptCode)
                    }
                    pendingSend.file
                }
                FileResponseData.RESPONSE_CODE_REJECT -> {
                    // Need do nothing
                }
            }
        }
    }

    private lateinit var broadcastReceiver: UDPReceiver
    private lateinit var messageReceiver: UDPReceiver

    private val udpSender by lazy { CatSender() }

    @Volatile
    private var isReady = false

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

    private val fileServer by lazy { FileServer.obtain(worker, outputDir) }

    private val pendingSendRegistry by lazy { PendingSendRegistry() }

    fun startup() {
        for (port in BROADCAST_PREPARE_PORTS) {
            val receiver = UDPReceiver(port)
            if (receiver.listen(broadcastHandler)) {
                if (!::broadcastReceiver.isInitialized) {
                    broadcastReceiver = receiver
                    break
                }
            }
        }
        for (port in MESSAGE_PREPARE_PORTS) {
            val receiver = UDPReceiver(port)
            if (receiver.listen(messageHandler)) {
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

    fun sendText(cat: Cat, text: String) {
        handler.post {
            udpSender.sendText(cat.ip, cat.messagePort, text)
        }
    }

    fun sendFileRequest(cat: Cat, file: IFile, observer: FileRequestObserver? = null) {
        observer?.run {
            handler.post {
                onParseStart()
            }
        }

        worker.queueWork({
            val fileReq = FileRequestData.from(file)
            pendingSendRegistry.request(fileReq, file)
            Protocol.fileRequest(fileReq)
        }) {
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
            udpSender.send(cat.ip, cat.messagePort, Protocol.fileResponse(fileReq.reject()))
        }
    }

    fun acceptFileRequest(cat: Cat, fileReq: FileRequestData) {
        handler.post {
            val acceptRes = fileReq.accept(fileServer.port)

            // record file that will receive
            fileServer.addFileInfo(acceptRes.acceptCode, fileReq.name, fileReq.size, fileReq.md5)

            fileServer.waitFile()
            udpSender.send(cat.ip, cat.messagePort, Protocol.fileResponse(acceptRes))
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

        fun broadcastMyself() {
            for (port in BROADCAST_PREPARE_PORTS) {
                udpSender.send(
                    BROADCAST_IP, port,
                    Protocol.hiToAll(broadcastReceiver.port, messageReceiver.port, systemInfo)
                )
            }
        }

        handler.post {
            catCallback?.onReady(Cat(myLocalIp, systemInfo.systemUserName, systemInfo.system,
                broadcastReceiver.port, messageReceiver.port))
            broadcastMyself()
        }

        handler.post(1000L) { broadcastMyself() }
        handler.post(2000L) { broadcastMyself() }

        isReady = true
    }

    interface Callback {
        fun onReady(myCat: Cat)
    }

}