package com.example.reminders.data

import com.example.reminders.util.IntervalUnit
import java.util.concurrent.TimeUnit

/**
 * Reminder data class
 * basic details: title, notes, time details
 * pre alarms: array of pre-alarms
 * repeating: details for repeating the alarms
 */
data class Reminder(
    var id: String = "",
    var title: String = "", var notes: String = "",
    var idAlarm: Int = 0,
    var year: Int = 0, var month: Int = 0, var day: Int = 0,
    var hour: Int = 0, var minute: Int = 0,
    var timeInMillis: Long = 0,
    var repeatingDetails: RepeatingDetails = RepeatingDetails(),
    var preAlarms: ArrayList<PreAlarm> = ArrayList()
)

// PreAlarm data class
data class PreAlarm(
    var timeInMillis: Long = 0,
    var idPreAlarm: Int = 0,
    var valueTimeUnit: Long = 0,
    var timeUnit: TimeUnit = TimeUnit.MINUTES
)

// RepeatingDetails data class
data class RepeatingDetails(
    var isRepeating: Boolean = false,
    var originalDay: Int = 0,
    var interval: Long = 0,
    var intervalUnit: IntervalUnit = IntervalUnit.DAY,
    var weekOptions: WeekDays = WeekDays()
)