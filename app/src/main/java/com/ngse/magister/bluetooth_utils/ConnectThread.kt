package com.ngse.magister.bluetooth_utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import com.ngse.magister.Constants.BLUETOOTH_UUID
import com.ngse.magister.MESSAGE_STATUS
import com.ngse.magister.MyBluetoothService
import com.ngse.magister.data.STATUS
import java.io.IOException


class ConnectThread(deviceAddress: String,val handler: Handler) : Thread() {

    private val TAG = ConnectThread::class.java.canonicalName

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        mDevice?.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
    }

    private val mDevice : BluetoothDevice? by lazy { mBluetoothAdapter.getRemoteDevice(deviceAddress) }

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()//get the mobile bluetooth device


    override fun run() {
        // Cancel discovery because it otherwise slows down the connection.
        mDevice?.fetchUuidsWithSdp()
        mBluetoothAdapter?.cancelDiscovery()
        try {

        mmSocket?.use { socket ->
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket.connect()

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            val readMsg = handler.obtainMessage(MESSAGE_STATUS, STATUS.CONNECTED to mmSocket)
            readMsg.sendToTarget()
        }
        } catch (e: IOException) {
            val readMsg = handler.obtainMessage(MESSAGE_STATUS, STATUS.FAILED_CONNECTION)
            readMsg.sendToTarget()
        }
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            mmSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the client socket", e)
        }
    }
}