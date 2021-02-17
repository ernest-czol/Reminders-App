package com.example.reminders.util

import com.example.reminders.constants.ConstantsTime
import java.util.concurrent.TimeUnit

object TimeUtil {
    fun computeTimeInMillis(option: String?): Long {
        val value = getValue(option)

        val timeUnit = getTimeUnit(option)
        return timeUnit.toMillis(value)
    }

    fun getValue(option: String?): Long {
        return option?.split(' ')?.get(0)?.toLong() ?: 0
    }

    fun getTimeUnit(option: String?): TimeUnit {
        val timeUnit = option?.split(' ')?.get(1)

        return when {
            timeUnit?.contains(ConstantsTime.MINUTE) == true -> TimeUnit.MINUTES
            timeUnit?.contains(ConstantsTime.HOUR) == true -> TimeUnit.HOURS
            timeUnit?.contains(ConstantsTime.DAY) == true -> TimeUnit.DAYS
            else -> TimeUnit.MILLISECONDS
        }
    }
}