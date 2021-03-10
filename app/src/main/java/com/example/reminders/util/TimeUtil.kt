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

    /**
     * Convert milliseconds to a date format
     */
    fun convertDate(timeInMillis: Long): String =
        DateFormat.format("dd/MM/yyyy HH:mm:ss", timeInMillis).toString()

    // Get hour from a string formatted as a date: 09/03/2021 06:03:00
    fun getHour(date: String): Int =
        date.split(' ')[1].split(':')[0].toInt()

    // Get day from a string formatted as a date: 09/03/2021 06:03:00
    fun getDay(date: String): Int =
        date.split(' ')[0].split('/')[0].toInt()

    // Get month from a string formatted as a date: 09/03/2021 06:03:00
    fun getMonth(date: String): Int =
        date.split(' ')[0].split('/')[1].toInt()

    // Get year from a string formatted as a date: 09/03/2021 06:03:00
    fun getYear(date: String): Int =
        date.split(' ')[0].split('/')[2].toInt()

    /**
     * Compute year
     * If the current month + the number of months > 12 -> increment the year
     */
    fun computeYear(intervalValueMonth: Int, currentMonth: Int, currentYear: Int) =
        if (currentMonth + intervalValueMonth > 12) currentYear + 1 else currentYear

    /**
     * Compute year
     * Return the month index, check if the addition exceeds 12
     */
    fun computeMonth(intervalValueMonth: Int, currentMonth: Int) = (currentMonth + intervalValueMonth) % 12

    /**
     * Compute day based on repetitive months
     */
    fun computeDay(intervalValueMonth: Int, currentMonth: Int, currentYear: Int, originalDay: Int): Int {
        // Compute year and month
        val year = computeYear(intervalValueMonth, currentMonth, currentYear)
        val month = computeMonth(intervalValueMonth, currentMonth)
        // Get the number of days of this month
        var monthNumberOfDays = Months.values()[month-1].days

        // Check if it is a leap year and month is february
        if (leapYear(year) && month == Months.FEBRUARY.ordinal + 1)
            monthNumberOfDays += 1

        // Check if the original day of the reminder fit in the month`s number of days
        return if (originalDay <= monthNumberOfDays)
            originalDay
        else
            monthNumberOfDays
    }

    /**
     * Compute day based on repetitive years
     */
    fun computeDay(originalDay: Int, currentMonth: Int, year: Int): Int {
        // Get the number of days of this month
        var monthNumberOfDays = Months.values()[currentMonth - 1].days

        // Check if it is a leap year and month is february
        if (leapYear(year) && currentMonth == Months.FEBRUARY.ordinal + 1)
            monthNumberOfDays += 1

        // Check if the original day of the reminder fit in the month`s number of days
        return if (originalDay <= monthNumberOfDays)
            originalDay
        else
            monthNumberOfDays
    }

    // Check the leap year
    private fun leapYear(year: Int) = ((year % 400) == 0) || (((year % 4) == 0) && ((year % 100) != 0))

}