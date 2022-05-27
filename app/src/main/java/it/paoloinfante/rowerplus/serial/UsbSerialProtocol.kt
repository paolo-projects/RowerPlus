package it.paoloinfante.rowerplus.serial

import android.content.Context
import android.hardware.usb.UsbDeviceConnection
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.RowerPull
import it.paoloinfante.rowerplus.usb.ErgometerDeviceListener
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.concurrent.thread

class UsbSerialProtocol(
    private val mContext: Context,
    private val serialPort: UsbSerialPort,
    connection: UsbDeviceConnection,
    private val rowerSerialDataListener: ErgometerDeviceListener
) : SerialInputOutputManager.Listener {
    companion object {
        private const val TAG = "RowerSerialMcu"

        private val PACKET_SIZE = 24
    }

    private val dataBuffer = ArrayList<Byte>()

    //private var serialInputOutputManager: SerialInputOutputManager
    private var serialRunning = true
    private var serialReadThread: Thread? = null

    init {
        Log.d(TAG, "Constructing Serial decoder")
        serialPort.open(connection)
        serialPort.setParameters(
            mContext.resources.getInteger(R.integer.serial_baud_rate),
            UsbSerialPort.DATABITS_8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE
        )
        //serialInputOutputManager = SerialInputOutputManager(serialPort, this)
    }

    fun start() {
        Log.d(TAG, "start: Starting serial decoder")
        stop()
        serialRunning = true
        serialReadThread = thread(
            start = true,
            isDaemon = false,
            contextClassLoader = null,
            name = null,
            priority = -1,
            block = serialReadRunnable
        )
        //serialInputOutputManager.start()
    }

    fun stop() {
        //serialInputOutputManager.stop()
        if (serialReadThread != null) {
            serialRunning = false
            serialReadThread?.join()
            serialReadThread = null
        }
    }

    private val serialReadRunnable = {
        while (serialRunning) {
            val buffer = ByteArray(24)
            try {
                val readBytes = serialPort.read(buffer, 100)
                dataBuffer.addAll(buffer.asList().subList(0, readBytes))

                while (dataBuffer.size > 0 &&
                    (String(byteArrayOf(dataBuffer[0]), Charsets.US_ASCII) != "B"
                            || (dataBuffer.size > 1 && String(
                        byteArrayOf(dataBuffer[1]),
                        Charsets.US_ASCII
                    ) != "T"))
                ) {
                    dataBuffer.removeAt(0)
                }

                if (dataBuffer.size >= PACKET_SIZE) {
                    val dataBufferArray = dataBuffer.subList(0, PACKET_SIZE).toByteArray()
                    val bt = String(dataBufferArray, 0, 2, Charsets.US_ASCII)
                    val et = String(dataBufferArray, 20, 2, Charsets.US_ASCII)
                    if (bt == "BT" && et == "ET") {
                        onNewData(dataBufferArray)
                    }
                    dataBuffer.subList(0, PACKET_SIZE).clear()
                }
            } /*catch (exc: TimeoutException) {
                // Do nothing
            }*/ catch (exc: IOException) {
                serialRunning = false
                serialReadThread = null
                onRunError(exc)
            }
        }
    }

    override fun onNewData(data: ByteArray?) {
        Log.d(TAG, "onNewData: Received ${data?.size} bytes of data")

        if (data != null) {
            attemptParseData(data)
        }
    }

    private fun attemptParseData(data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val energy = buffer.getFloat(4)
        val meanPower = buffer.getFloat(8)
        val distance = buffer.getFloat(12)
        val checksum = (buffer.getShort(16).toInt() and 0xFFFF).toUShort()

        var cmpChecksum: Int = 0
        for (i in 0..23) {
            if (i != 16 && i != 17) {
                cmpChecksum = (cmpChecksum + (buffer.get(i).toInt() and 0xFF)) and 0xFFFF
            }
        }

        if (cmpChecksum == checksum.toInt()) {
            val pull =
                RowerPull(
                    Date(),
                    energy,
                    meanPower,
                    distance
                )
            Log.d(TAG, "attemptParseData: received $pull")
            rowerSerialDataListener.onDeviceDataReceived(
                pull
            )
        }
    }

    override fun onRunError(e: Exception?) {
        e?.printStackTrace()
        stop()
        rowerSerialDataListener.onDeviceReadError(e)
    }
}