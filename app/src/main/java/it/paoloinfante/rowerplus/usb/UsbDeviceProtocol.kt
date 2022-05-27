package it.paoloinfante.rowerplus.usb

import android.content.Context
import android.hardware.usb.*
import android.util.Log
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.RowerPull
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

/*  TODO: The USB HID has to be tested
    Advantages over USB-UART: can use custom VID/PID and register
    only the specific device with the application
    The higher bandwidth allows sending raw angular velocity
    measurements and do the math inside the mobile application
 */
class UsbDeviceProtocol(
    private val mContext: Context,
    private val rowerDataListener: ErgometerDeviceListener
) {

    companion object {
        private const val TAG = "UsbDeviceProtocol"
        private const val PACKET_SIZE = 12
        private const val USB_READ_TIMEOUT = 100
    }

    private val deviceVid: Int = mContext.resources.getInteger(R.integer.custom_usb_vid)
    private val devicePid: Int = mContext.resources.getInteger(R.integer.custom_usb_pid)
    private val deviceClass: Int = mContext.resources.getInteger(R.integer.custom_usb_class)

    private var usbThreadRunning = true
    private var usbThread: Thread? = null

    private var usbDevice: UsbDevice? = null
    private var usbDeviceConnection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var usbEndpoint: UsbEndpoint? = null

    @Throws(UsbProtocolException::class)
    fun start(device: UsbDevice, connection: UsbDeviceConnection) {
        stop()

        usbDevice = device
        usbDeviceConnection = connection
        usbInterface = null
        usbEndpoint = null

        if (device.vendorId != deviceVid && device.productId != devicePid && device.deviceClass != deviceClass) {
            throw UsbProtocolException("Bad USB device. One between VID, PID or device class does not match")
        }

        if (usbDevice!!.configurationCount == 0) {
            throw UsbProtocolException("No USB Configurations found")
        }
        val configuration = usbDevice!!.getConfiguration(0)

        if (configuration.interfaceCount == 0) {
            throw UsbProtocolException("No USB Interfaces found in configuration")
        }

        usbInterface = configuration.getInterface(0)

        for (i in 0 until usbInterface!!.endpointCount) {
            val endpoint = usbInterface!!.getEndpoint(i)
            if (endpoint.direction == UsbConstants.USB_DIR_IN) {
                usbEndpoint = endpoint
                break
            }
        }

        if (usbEndpoint == null) {
            throw UsbProtocolException("No valid USB endpoint found")
        }
        if (usbEndpoint!!.maxPacketSize < PACKET_SIZE) {
            throw UsbProtocolException("USB Endpoint max packet size below packet size")
        }

        usbThreadRunning = true
        usbThread = thread(start = true, block = usbReadThread)
    }

    private val usbReadThread = {
        if (!usbDeviceConnection!!.claimInterface(usbInterface, true)) {
            rowerDataListener.onDeviceReadError(UsbProtocolException("Couldn't claim USB interface"))
        } else {
            //val buffer = ByteArray(usbEndpoint!!.maxPacketSize)
            val buffer = ByteBuffer.allocate(usbEndpoint!!.maxPacketSize)
            val bufferByteArray = ByteArray(PACKET_SIZE)

            try {
                while (usbThreadRunning) {
                    val usbRequest = UsbRequest()
                    usbRequest.initialize(usbDeviceConnection, usbEndpoint)
                    usbRequest.queue(buffer)

                    while (true) {
                        try {
                            if (usbDeviceConnection!!.requestWait(USB_READ_TIMEOUT.toLong()) == usbRequest) {
                                buffer.position(0)
                                buffer.get(bufferByteArray)
                                attemptParseData(bufferByteArray)
                                break
                            }
                        } catch (exc: Exception) {
                            when (exc) {
                                is TimeoutException,
                                is InterruptedException -> if (!usbThreadRunning) break
                                else -> throw exc
                            }
                        }
                    }
                    usbRequest.close()

                    // Wait before requesting the next packet
                    // according to the endpoint poll rate
                    Thread.sleep(usbEndpoint!!.interval.toLong() + 25)
                }
            } catch (exc: Exception) {
                rowerDataListener.onDeviceReadError(exc)
            } finally {
                usbDeviceConnection!!.releaseInterface(usbInterface)
                usbDeviceConnection!!.close()
            }
        }
    }

    private fun attemptParseData(data: ByteArray) {
        val buffer = ByteBuffer.wrap(data)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        val energy = buffer.float
        val power = buffer.float
        val distance = buffer.float

        if (energy > 0 && power > 0 && distance > 0) {
            val pull =
                RowerPull(
                    Date(),
                    energy,
                    power,
                    distance
                )
            Log.d(TAG, "attemptParseData: received $pull")
            rowerDataListener.onDeviceDataReceived(
                pull
            )
        }
    }

    fun stop() {
        usbThreadRunning = false
        usbThread?.join()
        usbThread = null
        usbDevice = null
        usbDeviceConnection = null
    }

    class UsbProtocolException(message: String) : Exception(message) {}
}