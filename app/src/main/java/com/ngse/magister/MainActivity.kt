package com.ngse.magister

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import kotlinx.android.synthetic.main.activity_led_control.*
import java.io.IOException
import com.anychart.data.Set
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import com.ngse.magister.bluetooth_utils.ConnectThreadAsync
import com.ngse.magister.data.CustomDataPoint
import com.ngse.magister.data.Parameters
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {


    var address: String? = null
    private var progress: ProgressDialog? = null
    var btSocket: BluetoothSocket? = null
    private var isBtConnected = false
    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var handler : Handler?=null
    var myBluetoothService : MyBluetoothService?=null
    var seriesData = ArrayList<CustomDataEntry>()
    lateinit var cartesian : Cartesian
    var seriesDataPointList = ArrayList<CustomDataPoint>()
    var series : LineGraphSeries<DataPointInterface> ?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = intent.getStringExtra(EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_led_control)
        //initNewChart()
        initChart()
        btnOn.setOnClickListener{ turnOnLed()}
        btnOff.setOnClickListener{ turnOffLed()  }
        btnDis.setOnClickListener{ Disconnect() }
        //val connectBt = ConnectBT()
        //connectBt.execute() //Call the class to connect
        ConnectThreadAsync(address!!){
            if (it== null) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
                finish()
            } else {
                btSocket = it
                msg("Connected.")
                isBtConnected = true
                openBT()
            }
            progress?.dismiss()
        }.execute()


    }

    fun initChart(){
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(10.0)
        graph.gridLabelRenderer.labelVerticalWidth = 100
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)
        series?.isDrawDataPoints = true
        series?.isDrawBackground = true
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.TOP

    }

    fun initNewChart(){
        cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)
            // TODO ystroke
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        cartesian.title("Trend of Sales of the Most Popular Products of ACME Corp.")

        cartesian.yAxis(0).title("Number of Bottles Sold (thousands)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        any_chart_view.setChart(cartesian)
    }

    @Throws(IOException::class)
    fun openBT() {
        handler = Handler {
            when (it.what){
                MESSAGE_READ -> {
                    if (it.obj is Parameters) {
                        val parameters = it.obj as Parameters
                        Constants.DATE_TIME_FORMAT_HOUR_MINUTE_SEC
                        seriesDataPointList.add(
                            CustomDataPoint(
                                Constants.DATE_TIME_FORMAT_HOUR_MINUTE_SEC_DOUBLE.format(Calendar.getInstance().time),
                                parameters.temperature.toDouble()
                            )
                        )
                        if (seriesDataPointList.size == 1) {
                            val seriesDataPoint = seriesDataPointList.toTypedArray()
                            series = LineGraphSeries(seriesDataPoint)
                            graph.addSeries(series)
                        }
                        else series?.appendData(seriesDataPointList.last(),true,22)

                        //makeChart(parameters)
                        progress_bar.visibility = View.GONE

                    }
                }
                MESSAGE_WRITE -> {}
                MESSAGE_TOAST -> {}
            }
            handler!!.sendEmptyMessage(0)
        }
        myBluetoothService = btSocket?.let { MyBluetoothService(handler!!, it) }



        Log.d("TAG","Bluetooth Opened")
    }

    fun makeChart(parameters: Parameters){
        seriesData.add(CustomDataEntry(Constants.DATE_TIME_FORMAT_HOUR_MINUTE_SEC.format(Calendar.getInstance().time), parameters.temperature, parameters.humidity))

        val set = Set.instantiate()
        set.data(seriesData as List<DataEntry>?)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        //val series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }")

        val series1 = cartesian.line(series1Mapping)
        series1.name("Temperature")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        val series2 = cartesian.line(series2Mapping)
        series2.name("Humidity")
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)


        /*val series3 = cartesian.line(series3Mapping)
        series3.name("Tequila")
        series3.hovered().markers().enabled(true)
        series3.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series3.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)*/
        progress_bar.visibility = View.GONE
    }


    data class CustomDataEntry(val x: String, val value: Number, val value2: Number/*, val value3: Number*/) : ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
            //setValue("value3", value3)
        }

    }

    private fun Disconnect() {
        if (btSocket != null)
        //If the btSocket is busy
        {
            try {
                btSocket?.close() //close connection
            } catch (e: IOException) {
                msg("Error")
            }

        }
        finish() //return to the first layout

    }

    private fun turnOffLed() {
        if (myBluetoothService != null) {
            try {
                //btSocket?.outputStream?.write("0".toByteArray())
                myBluetoothService?.write("0".toByteArray())
            } catch (e: IOException) {
                msg("Error")
            }

        }
    }

    private fun turnOnLed() {
        if (myBluetoothService != null) {
            try {
                //btSocket?.outputStream?.write("1".toByteArray())
                myBluetoothService?.write("1".toByteArray())
            } catch (e: IOException) {
                msg("Error")
            }

        }
    }

    // fast way to call Toast
    private fun msg(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_led_control, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    private inner class ConnectBT : AsyncTask<Void, Void, String>  // UI thread
        () {
        private var ConnectSuccess = true //if it's here, it's almost connected

        override fun onPreExecute() {
            progress = ProgressDialog.show(this@MainActivity, "Connecting...", "Please wait!!!")  //show a progress dialog
        }

        override fun doInBackground(vararg devices: Void) //while the progress dialog is shown, the connection is done in background
                : String? {
            try {
                if (btSocket == null || !isBtConnected) {
                    var myBluetooth = BluetoothAdapter.getDefaultAdapter()//get the mobile bluetooth device
                    val dispositivo =
                        myBluetooth.getRemoteDevice(address)//connects to the device's address and checks if it's available
                    dispositivo.fetchUuidsWithSdp()
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    //btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID)//create a RFCOMM (SPP) connection
                    btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID)//create a RFCOMM (SPP) connection

                    btSocket?.connect()//start connection
                }
            } catch (e: IOException) {
                ConnectSuccess = false//if the try failed, you can check the exception here
            }

            return ""
        }

        override fun onPostExecute(result: String) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result)

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
                finish()
            } else {
                msg("Connected.")
                isBtConnected = true
            }
            progress?.dismiss()
            openBT()
        }
    }


}

