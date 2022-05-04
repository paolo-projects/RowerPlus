package it.paoloinfante.rowerplus.serial

import android.content.Context
import android.hardware.usb.UsbDeviceConnection
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.util.SerialInputOutputManager
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.RowerPull
import java.util.*

class RowerSerialMcu(
    private val mContext: Context,
    serialPort: UsbSerialPort,
    connection: UsbDeviceConnection,
    private val rowerSerialDataListener: RowerSerialDataListener
) : SerialInputOutputManager.Listener {
    companion object {
        private const val TAG = "RowerSerialMcu"
    }

    private val dataBuffer = ArrayList<Byte>()

    interface RowerSerialDataListener {
        fun onDataReceived(pull: RowerPull)
        fun onError(e: Exception?)
    }

    private var serialInputOutputManager: SerialInputOutputManager

    init {
        Log.d(TAG, "Constructing Serial decoder")
        serialPort.open(connection)
        serialPort.setParameters(
            mContext.resources.getInteger(R.integer.serial_baud_rate),
            UsbSerialPort.DATABITS_8,
            UsbSerialPort.STOPBITS_1,
            UsbSerialPort.PARITY_NONE
        )
        serialInputOutputManager = SerialInputOutputManager(serialPort, this)
    }

    fun start() {
        Log.d(TAG, "start: Starting serial decoder")
        serialInputOutputManager.start()
    }

    fun stop() {
        serialInputOutputManager.stop()
    }

    override fun onNewData(data: ByteArray?) {
        Log.d(TAG, "onNewData: Received ${data?.size} bytes of data")

        data?.forEach {
            dataBuffer.add(it)
        }

        attemptParseData()
    }

    private fun attemptParseData() {
        var toRemove = 0

        for(i in 0..dataBuffer.size) {
            if (dataBuffer[i] != 'S'.code.toByte()) {
                toRemove++
            } else {
                break
            }
        }

        if(toRemove > 0) {
            for (i in 0..toRemove) {
                dataBuffer.removeAt(0)
            }
        }

        while (dataBuffer.size >= 9 && dataBuffer[0] == 'S'.code.toByte() && dataBuffer[1] == 'T'.code.toByte()) {
            val dataBA = dataBuffer.toByteArray()
            val avgNum = dataBA.getUIntAt(2).swapEndianness()
            val avgCount = dataBA[6].toInt() and 0xFF

            rowerSerialDataListener.onDataReceived(
                RowerPull(
                    Date(),
                    avgNum.toFloat() / avgCount,
                    avgCount
                )
            )

            dataBuffer.subList(0, 9).clear()
        }
    }

    fun ByteArray.getUIntAt(idx: Int) =
        ((this[idx].toUInt() and 0xFFu) shl 24) or
                ((this[idx + 1].toUInt() and 0xFFu) shl 16) or
                ((this[idx + 2].toUInt() and 0xFFu) shl 8) or
                (this[idx + 3].toUInt() and 0xFFu)

    fun UInt.swapEndianness() =
        ((this and 0xFF000000u) shr 24) or
                ((this and 0x00FF0000u) shr 8) or
                ((this and 0x0000FF00u) shl 8) or
                ((this and 0x000000FFu) shl 24)

    override fun onRunError(e: Exception?) {
        e?.printStackTrace()
        rowerSerialDataListener.onError(e)
    }
}