package com.ngse.magister.activities

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.ngse.magister.R
import com.ngse.magister.bluetooth_utils.*
import com.ngse.magister.data.Parameters
import kotlinx.android.synthetic.main.activity_control.*
import kotlinx.android.synthetic.main.activity_control_humidity.*


class ControlHumidityActivity : AppCompatActivity() {

    //private var progress: ProgressDialog? = null
    var myBluetoothService : ReadWriteInteraction?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = intent.getStringExtra(EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_control_humidity)
        //progress = ProgressDialog.show(this, "Connecting...", "Please wait!!!")  //show a progress dialog

        ConnectThreadAsync(address){ btSocket ->
            if (btSocket== null) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
                finish()
            } else {
                msg("Connected.")
                myBluetoothService = ReadWriteInteraction(handler, btSocket).apply { start() }
            }
            //progress?.dismiss()
        }.execute()

        applyButton.setOnClickListener { sendLedState(inputHumidityEditText.text.toString().toIntOrNull()) }
        disconnectButton.setOnClickListener { disconnect() }
    }


    private val handler = Handler {
        when (it.what){
            MESSAGE_READ -> {
                if (it.obj is Parameters) {
                    val parameters = it.obj as Parameters
                    //makeChart(parameters)
                    progress_bar.visibility = View.GONE

                }
            }
            MESSAGE_WRITE -> {}
            MESSAGE_TOAST -> { }
        }
        true
    }

    override fun onDestroy() {
        super.onDestroy()
        myBluetoothService?.cancel()
    }

    private fun disconnect() {
        myBluetoothService?.cancel()
        finish() //return to the first layout
    }

    private fun sendLedState(ledState : Int?){
        ledState?.let { myBluetoothService?.write(it) }
    }

    // fast way to call Toast
    private fun msg(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }


}

