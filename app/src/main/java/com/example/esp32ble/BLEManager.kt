package com.example.esp32ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID
import java.util.LinkedList
import java.util.Queue
class BLEManager(
    private val context: Context,
    private val viewModel: BLEViewModel
){

    companion object {
        private const val TAG = "BLE"

        private val SERVICE_UUID =
            UUID.fromString("12345678-1234-1234-1234-123456789001")

        private val CHAR_UUID =
            UUID.fromString("12345678-1234-1234-1234-123456789002")

        private val CCCD_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

    private val scanner: BluetoothLeScanner?
        get() = bluetoothAdapter.bluetoothLeScanner

    private var bluetoothGatt: BluetoothGatt? = null
    private var dataCharacteristic: BluetoothGattCharacteristic? = null

    //----------------------------------------------------
    // BLE Write Queue
    //----------------------------------------------------

    private val writeQueue: Queue<String> = LinkedList()

    private var writeBusy = false

    private var rxBuffer = StringBuilder()
    private var writeTimeoutRunnable: Runnable? = null   // <-- add this line
    private var expectedPackets = 0
    private var currentPacket = 0
    private var currentType = ""
    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled

    private val deviceInfo = DeviceInfo()

    var onDeviceFound: ((ScannedDevice) -> Unit)? = null
    var onConnectionChanged: ((Boolean) -> Unit)? = null
    var onTimerSaved: (() -> Unit)? = null
    var onRtcUpdated: (() -> Unit)? = null
    // ---- Runtime permission helpers ----
    private fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission(): Boolean =
        hasPermission(Manifest.permission.BLUETOOTH_SCAN)

    private fun hasConnectPermission(): Boolean =
        hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    // ---- Scanning ----

    fun startScan() {
        if (!bluetoothAdapter.isEnabled) {
            Log.e(TAG, "Bluetooth OFF")
            return
        }

        if (!hasScanPermission()) {
            Log.e(TAG, "Missing BLUETOOTH_SCAN permission, cannot start scan")
            return
        }

        Log.d(TAG, "Scanning Started")
        try {
            scanner?.startScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on startScan: ${e.message}")
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (hasScanPermission()) {
                try {
                    scanner?.stopScan(scanCallback)
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException on stopScan: ${e.message}")
                }
            }
            Log.d(TAG, "Scanning Finished")
        }, 10000)
    }

    fun stopScan() {
        if (!hasScanPermission()) return
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on stopScan: ${e.message}")
        }
    }

    // ---- Connection ----

    fun connect(device: BluetoothDevice) {
        if (!hasConnectPermission()) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission, cannot connect")
            return
        }

        Log.d(TAG, "Connecting to ${device.address}")

        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            dataCharacteristic = null

            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on connect: ${e.message}")
        }
    }

    fun disconnect() {
        if (!hasConnectPermission()) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission, cannot disconnect")
            return
        }
        try {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on disconnect: ${e.message}")
        }
        bluetoothGatt = null
        dataCharacteristic = null
        Log.d(TAG, "Disconnected")
    }

//----------------------------------------------------
// Queue Write
//----------------------------------------------------

    private fun processQueue()
    {
        if (writeBusy)
            return

        if (writeQueue.isEmpty())
            return

        val cmd = writeQueue.poll()

        if (cmd != null)
        {
            writeBusy = true
            write(cmd)

            // Safety timeout: if the GATT ack never arrives, don't jam the queue forever
            writeTimeoutRunnable = Runnable {
                if (writeBusy) {
                    Log.e(TAG, "Write ack timed out, resetting queue")
                    writeBusy = false
                    processQueue()
                }
            }
            Handler(Looper.getMainLooper()).postDelayed(writeTimeoutRunnable!!, 3000)
        }
    }

    fun queueWrite(text: String)
    {
        writeQueue.offer(text)
        processQueue()
    }



    fun write(text: String) {
        if (!hasConnectPermission()) {
            Log.e(TAG, "Missing BLUETOOTH_CONNECT permission, cannot write")
            return
        }

        val gatt = bluetoothGatt ?: return
        val characteristic = dataCharacteristic ?: return

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        characteristic.value = text.toByteArray()

        try {
            if (gatt.writeCharacteristic(characteristic)) {
                Log.d(TAG, "Sent : $text")
            } else {
                Log.e(TAG, "Write Request Failed")

                writeBusy = false
                processQueue()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException on write: ${e.message}")
        }
    }


    fun sendTimer(timer: TimerData) {

        val cmd =
            "SAVE_TIMER," +
                    "${timer.index}," +
                    "${timer.name}," +
                    "${if (timer.enable) 1 else 0}," +
                    "${timer.relay}," +
                    "${timer.onHour}," +
                    "${timer.onMinute}," +
                    "${timer.offHour}," +
                    "${timer.offMinute}," +
                    "${timer.days}"

        Log.d(TAG, "TX Timer = $cmd")

        queueWrite(cmd)
    }

    fun setRelayMode(relay: Int, mode: Int) {
        Log.d("BLE", "Sending RELAY,$relay,$mode")
        queueWrite("RELAY,$relay,$mode")
    }


    // ---- GATT callback ----

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Connection Error : $status")
                if (hasConnectPermission()) {
                    try {
                        gatt.close()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException on close: ${e.message}")
                    }
                }
                bluetoothGatt = null
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                onConnectionChanged?.invoke(true)

                bluetoothGatt = gatt

                Log.d(TAG, "Connected")
                if (hasConnectPermission()) {
                    try {
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException on discoverServices: ${e.message}")
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                onConnectionChanged?.invoke(false)

                Log.d(TAG, "Disconnected")
                dataCharacteristic = null
                if (hasConnectPermission()) {
                    try {
                        gatt.close()
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException on close: ${e.message}")
                    }
                }
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service Discovery Failed : $status")
                return
            }

            if (!hasConnectPermission()) {
                Log.e(TAG, "Missing BLUETOOTH_CONNECT permission during service discovery")
                return
            }

            val service = gatt.getService(SERVICE_UUID)
            if (service == null) {
                Log.e(TAG, "Service Not Found")
                return
            }

            Log.d(TAG, "Service Found")

            val characteristic = service.getCharacteristic(CHAR_UUID)
            if (characteristic == null) {
                Log.e(TAG, "Characteristic Not Found")
                return
            }

            Log.d(TAG, "Characteristic Found")

            dataCharacteristic = characteristic

            try {
                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(CCCD_UUID)
                if (descriptor == null) {
                    Log.e(TAG, "CCCD Descriptor Not Found")
                    return
                }

                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException during service setup: ${e.message}")
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Notifications Enabled")

                queueWrite("GET_INFO")

                Handler(Looper.getMainLooper()).postDelayed({
                    queueWrite("GET_TIME")
                }, 300)

                Handler(Looper.getMainLooper()).postDelayed({
                   queueWrite("GET_ALL")
                }, 600)
            } else {
                Log.e(TAG, "Descriptor Write Failed")
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            writeTimeoutRunnable?.let {
                Handler(Looper.getMainLooper()).removeCallbacks(it)
            }
            writeTimeoutRunnable = null

            writeBusy = false

            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                Log.d(TAG, "Write Successful")
            }
            else
            {
                Log.e(TAG, "Write Failed")
            }

            processQueue()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {

            val packet = String(value)

            Log.d(TAG, "RX : $packet")

            val parts = packet.split("|", limit = 4)
            Log.d(TAG, "parts = ${parts.joinToString(" | ")}")

            if (parts.size < 4) {
                Log.e(TAG, "Invalid Packet")
                return
            }

            val type = parts[0]
            val packetNo = parts[1].toInt()
            val totalPackets = parts[2].toInt()
            val data = parts[3]

// First packet
            if (packetNo == 1) {
                rxBuffer.clear()
                currentType = type
                expectedPackets = totalPackets
                currentPacket = 1
            } else if (type != currentType || packetNo != currentPacket + 1) {
                // Out-of-sequence or interleaved packet from a different message — resync
                Log.e(TAG, "Out-of-sequence packet: type=$type expected=$currentType packetNo=$packetNo expectedNo=${currentPacket + 1}")
                rxBuffer.clear()
                if (packetNo == 1) {
                    currentType = type
                    expectedPackets = totalPackets
                    currentPacket = 1
                } else {
                    return // drop stray packet, wait for the next packetNo==1 to resync
                }
            }

            currentPacket = packetNo

            Log.d(TAG, "Appending = '$data'")
            rxBuffer.append(data)
            Log.d(TAG, "Buffer = '$rxBuffer'")

            Log.d(TAG, "Packet $packetNo / $totalPackets")

            if (packetNo == totalPackets) {

                Log.d(TAG, "Complete $currentType : ${rxBuffer}")

                when (currentType) {

                    "TIME" -> {

                        val response = rxBuffer.toString()

                        if (response == "OK") {

                            Log.d(TAG, "RTC Updated")

                            onRtcUpdated?.invoke()

                            Handler(Looper.getMainLooper()).postDelayed({
                                queueWrite("GET_TIME")
                            }, 200)

                        } else {

                            val p = response.split(",")

                            if (p.size >= 6) {

                                deviceInfo.rtcTime =
                                    "${p[2]}-${p[1]}-${p[0]}  ${p[3]}:${p[4]}:${p[5]}"

                                viewModel.update(deviceInfo)
                            }
                        }
                    }

                    "INFO" -> {

                        val p = rxBuffer.toString().split(",")

                        deviceInfo.deviceName = p[0]

                        if (p.size > 1)
                            deviceInfo.firmware = p[1]

                        viewModel.update(deviceInfo)

                        Log.d(TAG, deviceInfo.toString())

                    }

                    "STATUS" ->
                    {
                        val p = rxBuffer.toString().split(",")

                        if (p.size >= 4)
                        {
                            deviceInfo.relay1 = p[0] == "1"
                            deviceInfo.relay2 = p[1] == "1"

                            deviceInfo.relay1Mode = p[2].toInt()
                            deviceInfo.relay2Mode = p[3].toInt()

                            viewModel.update(deviceInfo)
                        }
                    }

                   // BLEManager.kt — remove the postDelayed GET_STATUS in the "RELAY" case
                    "RELAY" -> {
                        Log.d(TAG, "Relay Result = ${rxBuffer}")
                        // no extra GET_STATUS needed — bleLoop() already pushes STATUS every second
                    }

                    "TIMER" -> {

                        try {

                            val timer = parseTimer(rxBuffer.toString())

                            deviceInfo.timers[timer.index] = timer

                            viewModel.update(deviceInfo)

                            Log.d(TAG, timer.toString())

                        } catch (e: Exception) {

                            Log.e(TAG, "Timer Parse Error : ${e.message}")

                        }



                    }

                    "SAVE" -> {

                        Log.d(TAG, "Save Result = ${rxBuffer}")

                        onTimerSaved?.invoke()

                        Handler(Looper.getMainLooper()).postDelayed({

                            queueWrite("GET_ALL")

                        }, 300)

                    }

                    "ERROR" -> {

                        Log.e(TAG, rxBuffer.toString())

                    }

                }

            }

        }
    }


    private fun parseTimer(data: String): TimerData {

        val p = data.split(",")

        if (p.size != 9)
            throw IllegalArgumentException("Invalid timer data: $data")

        return TimerData(
            index = p[0].toInt(),
            name = p[1],
            enable = p[2] == "1",
            relay = p[3].toInt(),
            onHour = p[4].toInt(),
            onMinute = p[5].toInt(),
            offHour = p[6].toInt(),
            offMinute = p[7].toInt(),
            days = p[8].toInt()
        )
    }

    // ---- Scan callback ----

    private val scanCallback = object : ScanCallback() {

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan Failed : $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!hasConnectPermission()) {
                // Reading device.name requires BLUETOOTH_CONNECT on some OEMs
                return
            }

            try {
                Log.d(TAG, "Device: ${result.device.name} ${result.device.address} RSSI=${result.rssi}")

                onDeviceFound?.invoke(
                    ScannedDevice(
                        result.device,
                        result.device.name ?: "Unknown Device",
                        result.device.address,
                        result.rssi
                    )
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException on scan result: ${e.message}")
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {

            if (!hasConnectPermission()) return

            try {

                results.forEach {

                    onDeviceFound?.invoke(
                        ScannedDevice(
                            it.device,
                            it.device.name ?: "Unknown Device",
                            it.device.address,
                            it.rssi
                        )
                    )

                }

            } catch (e: SecurityException) {

                Log.e(TAG, "SecurityException on batch scan results: ${e.message}")

            }
        }
    }
}