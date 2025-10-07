package com.vigram.sdkgh1.helpers


import java.util.Date

private const val MILL_IN_DAY = 24 * 60 * 60 * 1000
private const val MILL_IN_HOURS = 60 * 60 * 1000
private const val MILL_IN_MIN = 60 * 1000
private const val MILL_IN_SEC = 1000

object TimeUtil {
    fun getCurrentTimeInString(): String {
        val curMill = Date().time % MILL_IN_DAY
        val hours = curMill / MILL_IN_HOURS
        val min = (curMill - hours * MILL_IN_HOURS) / MILL_IN_MIN
        val sec = ((curMill - hours * MILL_IN_HOURS) - min * MILL_IN_MIN) / MILL_IN_SEC
        val millisec = ((curMill - hours * MILL_IN_HOURS) - min * MILL_IN_MIN) - sec * MILL_IN_SEC
        val timeInString = StringBuilder()
        if (hours.toString().length == 1) timeInString.append("0$hours:") else timeInString.append("$hours:")
        if (min.toString().length == 1) timeInString.append("0$min:") else timeInString.append("$min:")
        if (sec.toString().length == 1) timeInString.append("0$sec.") else timeInString.append("$sec.")
        if (millisec.toString().length == 1) timeInString.append("00$millisec")
        else if (millisec.toString().length == 2) timeInString.append("0$millisec")
        else timeInString.append(millisec)

        return timeInString.toString()
    }
}