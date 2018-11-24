package com.ngse.magister

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.ngse.magister.data.Parameters
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

private const val TAG = "MY_APP_DEBUG_TAG"

// Defines several constants used when transmitting messages between the
// service and the UI.
const val MESSAGE_STATUS: Int = -1
const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
// ... (Add other message types here as needed.)

class MyBluetoothService(
    // handler that gets info from Bluetooth service
    private val mHandler: Handler,
    mmSocket: BluetoothSocket
) {
    private var connectedThread : ConnectedThread
    init {
        connectedThread = ConnectedThread(mmSocket).apply { start() }
    }

    fun write(bytes: ByteArray){
        connectedThread.write(bytes)
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream
        private val delimiter: Byte = 10 //This is the ASCII code for a newline character


        override fun run() {
            var numBytes: Int // bytes returned from read()
            var readBufferPosition = 0


            // Keep listening to the InputStream until an exception occurs.
            //while (true) {
            while (!Thread.currentThread().isInterrupted) {
                // Read from the InputStream.
                try {
                    //mmInStream.read(mmBuffer)
                    val isConnected = mmSocket.isConnected
                    val bytesAvailable = mmInStream.available()
                    if (bytesAvailable > 0) {
                        val packetBytes = ByteArray(bytesAvailable)
                        mmInStream.read(packetBytes)
                        for (i in 0 until bytesAvailable) {
                            val b = packetBytes[i]
                            if (b == delimiter) {
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(mmBuffer, 0, encodedBytes, 0, encodedBytes.size)
                                val data = String(encodedBytes, Charset.forName("US-ASCII"))
                                Log.d(TAG, data)
                                val gson = GsonBuilder().setLenient().create()
                                val parameters = try {
                                    gson.fromJson<Parameters>(data, Parameters::class.java)
                                } catch (e: Exception) {
                                    when (e) {
                                        is IllegalStateException -> e.printStackTrace()
                                        is JsonIOException -> e.printStackTrace()
                                        else -> e.printStackTrace()
                                    }
                                }
                                readBufferPosition = 0
                                val readMsg = mHandler.obtainMessage(
                                    MESSAGE_READ, parameters
                                )
                                readMsg.sendToTarget()

                            } else {
                                mmBuffer[readBufferPosition++] = b
                            }
                        }
                    }

                    // Send the obtained bytes to the UI activity.
                    /* val readMsg = mHandler.obtainMessage(
                         MESSAGE_READ, numBytes, -1,
                         mmBuffer)
                     readMsg.sendToTarget()*/
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                val writeErrorMsg = mHandler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                mHandler.sendMessage(writeErrorMsg)
                return
            }

            // Share the sent message with the UI activity.
            val writtenMsg = mHandler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer
            )
            writtenMsg.sendToTarget()
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }
}