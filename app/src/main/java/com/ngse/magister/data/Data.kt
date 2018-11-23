package com.ngse.magister.data

import com.jjoe64.graphview.series.DataPointInterface
import java.io.Serializable


data class Parameters(val temperature : Int, val humidity : Int)

class CustomDataPoint(private var x: String, private var y: Double) : DataPointInterface, Serializable {
    override fun getX(): Double = x.toDouble()
    override fun getY(): Double = y
    override fun toString(): String = "[$x/$y]"
}

enum class STATUS{
    FAILED_CONNECTION,
    CONNECTED;
    companion object : List<STATUS> by STATUS.values().toList()
}