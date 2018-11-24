package com.ngse.magister

import java.text.SimpleDateFormat
import java.util.*

object Constants {

    val DATE_TIME_FORMAT_HOUR_MINUTE_SEC = SimpleDateFormat("HH:mm::ss", Locale.getDefault())
    val DATE_TIME_FORMAT_HOUR_MINUTE_SEC_DOUBLE = SimpleDateFormat("mm.ss", Locale.getDefault())
    val BLUETOOTH_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


}