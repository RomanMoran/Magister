package com.ngse.magister.bluetooth_utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import com.ngse.magister.Constants.BLUETOOTH_UUID
import java.io.IOException


class ConnectThreadAsync(deviceAddress : String,var runSocket: (socket: BluetoothSocket?) -> Unit) : AsyncTask<String, BluetoothDevice, BluetoothSocket>() {

    private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        mDevice?.createRfcommSocketToServiceRecord(BLUETOOTH_UUID)
    }

    private val mDevice : BluetoothDevice? by lazy { mBluetoothAdapter.getRemoteDevice(deviceAddress) }

    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun doInBackground(vararg adress: String?): BluetoothSocket? {
        mDevice?.fetchUuidsWithSdp()
        mBluetoothAdapter?.cancelDiscovery()
        try {
            return mmSocket?.apply { connect() }
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