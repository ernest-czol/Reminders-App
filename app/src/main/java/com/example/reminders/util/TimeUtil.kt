package com.example.reminders.util

import android.text.format.DateFormat
import com.example.reminders.constants.ConstantsTime
import java.util.concurrent.TimeUnit

object TimeUtil {
    /**
     * Convert a string to milliseconds
     */
    fun computeTimeInMillis(option: String?): Long {
        val value = getValue(option)

        val timeUnit = getTimeUnit(option)
        return timeUnit.toMillis(value)
    }

    /**
     * Get the first element of the string and convert it to Long (Exp: "12 hours" => 12)
     */
    fun getValue(option: String?): Long {
        return option?.split(' ')?.get(0)?.toLong() ?: 0
    }

    /**
     * Get the second element of the string and convert it to a time unit (Exp: "12 hours" => TimeUnit.HOURS)
     */
    fun getTimeUnit(option: String?): TimeUnit {
        val timeUnit = option?.split(' ')?.get(1)

        return when {
            timeUnit?.contains(ConstantsTime.MINUTE) == true -> TimeUnit.MINUTES
            timeUnit?.contains(ConstantsTime.HOUR) == true -> TimeUnit.HOURS
            timeUnit?.contains(ConstantsTime.DAY) == true -> TimeUnit.DAYS
            else -> TimeUnit.MILLISECONDS
        }
    }

    /**
     * Get the second element of the string and convert it to an interval unit (Exp: "15 weeks" => IntervalUnit.WEEK)
     */
    fun getIntervalUnit(option: String?): IntervalUnit {
        val timeUnit = option?.split(' ')?.get(1)

        return when {
            timeUnit?.contains(ConstantsTime.HOUR) == true -> IntervalUnit.HOUR
            timeUnit?.contains(ConstantsTime.DAY) == true -> IntervalUnit.DAY
            timeUnit?.contains(ConstantsTime.WEEK) == true -> IntervalUnit.WEEK
            timeUnit?.contains(ConstantsTime.MONTH) == true -> IntervalUnit.MONTH
            timeUnit?.contains(ConstantsTime.YEAR) == true -> IntervalUnit.YEAR
            else -> IntervalUnit.DAY
        }
    }

    private fun convertDate(timeInMillis: Long): String =
        DateFormat.format("dd/MM/yyyy hh:mm:ss", timeInMillis).toString()
//
//    fun computeNextHour(interval: Int, currentHour: Int): Int {
//        return (currentHour + interval) % 24
//    }
//
//    fun computeNextDay(interval: Int, currentDay: Int, currentMonth: Int, currentYear: Int): Int {
//        if (interval + currentDay <= Months.values()[currentMonth].days)
//            return currentDay + interval
//        else if (cu)
//    }
//
//    fun checkLeapYear(year: Int): Boolean {
//        return if (year % 4 == 0) {
//            if (year % 100 == 0) {
//                year % 400 == 0
//            } else
//                true
//        } else
//            false
//    }
}