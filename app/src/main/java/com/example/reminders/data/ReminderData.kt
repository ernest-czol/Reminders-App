package com.example.reminders.data

import java.util.concurrent.TimeUnit

data class Reminder(
    var title: String = "", var notes: String = "",
    var idAlarm: Int = 0,
    var year: Int = 0, var month: Int = 0, var day: Int = 0,
    var hour: Int = 0, var minute: Int = 0,
    var timeInMillis: Long = 0,
    var preAlarms: ArrayList<PreAlarm> = ArrayList()
)

data class PreAlarm(
    var timeInMillis: Long = 0,
    var idPreAlarm: Int = 0,
    var valueTimeUnit: Long = 0,
    var timeUnit: TimeUnit = TimeUnit.MINUTES
)