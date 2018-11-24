package com.ngse.magister.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_control.*
import com.anychart.data.Set
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPointInterface
import com.jjoe64.graphview.series.LineGraphSeries
import com.ngse.magister.Constants
import com.ngse.magister.R
import com.ngse.magister.bluetooth_utils.*
import com.ngse.magister.data.CustomDataPoint
import com.ngse.magister.data.LedState
import com.ngse.magister.data.Parameters
import java.util.*
import kotlin.collections.ArrayList


class ControlActivity : AppCompatActivity() {

    private var progress: ProgressDialog? = null
    var myBluetoothService : ReadWriteInteraction?=null
    var seriesData = ArrayList<CustomDataEntry>()
    lateinit var cartesian : Cartesian
    var seriesDataPointList = ArrayList<CustomDataPoint>()
    var series : LineGraphSeries<DataPointInterface> ?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = intent.getStringExtra(EXTRA_ADDRESS) //receive the address of the bluetooth device
        setContentView(R.layout.activity_control)
        //initNewChart()
        initChart()
        btnOn.setOnClickListener{ sendLedState(LedState.ON)}
        btnOff.setOnClickListener{ sendLedState(LedState.OFF)  }
        btnDis.setOnClickListener{ disconnect() }
        progress = ProgressDialog.show(this, "Connecting...", "Please wait!!!")  //show a progress dialog

        ConnectThreadAsync(address){ btSocket ->
            if (btSocket== null) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.")
                finish()
            } else {
                msg("Connected.")
                myBluetoothService = ReadWriteInteraction(handler, btSocket).apply { start() }
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


    private val handler = Handler {
        when (it.what){
            MESSAGE_READ -> {
                if (it.obj is Parameters) {
                    val parameters = it.obj as Parameters
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
            MESSAGE_TOAST -> { }
        }
        true
    }

    fun makeChart(parameters: Parameters){
        seriesData.add(
            CustomDataEntry(
                Constants.DATE_TIME_FORMAT_HOUR_MINUTE_SEC.format(
                    Calendar.getInstance().time
                ), parameters.temperature, parameters.humidity
            )
        )

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

        progress_bar.visibility = View.GONE
    }


    data class CustomDataEntry(val x: String, val value: Number, val value2: Number/*, val value3: Number*/) : ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        myBluetoothService?.cancel()
    }

    private fun disconnect() {
        myBluetoothService?.cancel()
        finish() //return to the first layout
    }

    private fun sendLedState(ledState : LedState){
        myBluetoothService?.write(ledState)
    }


    // fast way to call Toast
    private fun msg(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }


}

