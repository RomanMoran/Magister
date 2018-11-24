package com.ngse.magister.bluetooth_utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Handler
import android.text.Editable
import android.util.Log
import com.ngse.magister.Constants.BLUETOOTH_UUID
import com.ngse.magister.MESSAGE_STATUS
import com.ngse.magister.MyBluetoothService
import com.ngse.magister.data.STATUS
import java.io.IOException


class ConnectThreadAsync(deviceAddress : String,var runSocket: (socket: BluetoothSocket?) -> Unit) : AsyncTask<String, BluetoothDevice, BluetoothSocket>() {

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        mDevice?.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
    }

    private val mDevice : BluetoothDevice? by lazy { mBluetoothAdapter.getRemoteDevice(deviceAddress) }

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()//get the mobile bluetooth device


    override fun doInBackground(vararg adress: String?): BluetoothSocket? {
        mDevice?.fetchUuidsWithSdp()
        mBluetoothAdapter?.cancelDiscovery()
        try {

            mmSocket?.connect()
            val isConnected = mmSocket?.isConnected
            return mmSocket

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: BluetoothSocket?) {
        super.onPostExecute(result)
        runSocket.invoke(result)
    }



}