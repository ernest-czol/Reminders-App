package com.example.reminders.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_EXACT
import com.example.reminders.constants.ConstantsAlarm.EXACT_ALARM_TIME
import com.example.reminders.constants.ConstantsAlarm.ID_ALARM
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsReminder.DESCRIPTION_REMINDER
import com.example.reminders.constants.ConstantsReminder.TITLE_REMINDER
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.receiver.AlarmReceiver
import com.example.reminders.util.IntervalUnit
import com.example.reminders.util.TAG
import java.util.concurrent.TimeUnit

class AlarmService(private val context: Context) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    /**
     * Set an alarm at exact time
     */
    fun setExactAlarm(timeInMillis: Long, idAlarm: Int, title: String, description: String) {
        setAlarm(
            timeInMillis,
            getPendingIntent(
                getIntent().apply {
                    action = ACTION_SET_EXACT
                    putExtra(EXACT_ALARM_TIME, timeInMillis)
                    putExtra(TITLE_REMINDER, title)
                    putExtra(DESCRIPTION_REMINDER, description)
                    putExtra(ID_ALARM, idAlarm)
                },
                idAlarm
            )
        )
    }

    /**
     * Set repeating alarm
     */
    fun setRepeatingAlarm(reminder: Reminder) {
        val intervalUnit = reminder.repeatingDetails.intervalUnit
        val intervalValue = reminder.repeatingDetails.interval

        when(intervalUnit) {
            IntervalUnit.HOUR -> {
               reminder.timeInMillis += TimeUnit.HOURS.toMillis(intervalValue.toLong())

                Log.d(TAG, "din $intervalValue in $intervalValue $intervalUnit")

//                reminder.hour = computeNextHour(intervalValue, reminder.hour)
//                reminder.day = computeNextDay(intervalValue, reminder.hour, reminder.day, reminder.month, reminder.year)
//                reminder.month = computeNextMonth(intervalValue, reminder.hour, reminder.day, reminder.month, reminder.year)
//                reminder.year = computeNextYear(intervalValue, reminder.hour, reminder.day, reminder.month, reminder.year)
            }
            IntervalUnit.DAY -> {
                reminder.timeInMillis += TimeUnit.DAYS.toMillis(intervalValue.toLong())

                Log.d(TAG, "din $intervalValue in $intervalValue $intervalUnit")

//                reminder.day = computeNextDay(intervalValue, reminder.day, reminder.month, reminder.year)
//                reminder.month = computeNextMonth(intervalValue, reminder.day, reminder.month, reminder.year)
//                reminder.year = computeNextYear(intervalValue, reminder.day, reminder.month, reminder.year)
            }
            IntervalUnit.WEEK -> {
                Log.d(TAG, "din $intervalValue in $intervalValue $intervalUnit")
            }
            IntervalUnit.MONTH -> {
                Log.d(TAG, "din $intervalValue in $intervalValue $intervalUnit")
            }
            IntervalUnit.YEAR -> {
                Log.d(TAG, "din $intervalValue in $intervalValue $intervalUnit")
            }
        }

        setExactAlarm(reminder.timeInMillis, reminder.idAlarm, reminder.title, reminder.notes)
    }

    /**
     * Set alarm
     */
    private fun setAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        alarmManager?.let {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Delete an alarm
     */
    fun deleteAlarm(idAlarm: Int) {
        val intent = getIntent().apply {
            action = ACTION_SET_EXACT
        }

        alarmManager?.cancel(getPendingIntent(intent, idAlarm))
    }

    /**
     * Delete pre-alarms
     */
    private fun deletePreAlarms(preAlarms: ArrayList<PreAlarm>) {
        val intent = getIntent().apply {
            action = ACTION_SET_EXACT
        }

        for (preAlarm in preAlarms) {
            alarmManager?.cancel(getPendingIntent(intent, preAlarm.idPreAlarm))
        }
    }

    /**
     * Delete alarm and pre-alarms
     */
    fun deleteAlarmAndPreAlarms(idAlarm: Int?, preAlarms: ArrayList<PreAlarm>?) {
        idAlarm?.let{deleteAlarm(it)}
        preAlarms?.let{deletePreAlarms(it)}
    }

    /**
     * Update an alarm
     */
    fun updateAlarm(idAlarm: Int, timeInMillis: Long, title: String, description: String) {
        deleteAlarm(idAlarm)

        if (timeInMillis > System.currentTimeMillis())
            setExactAlarm(timeInMillis, idAlarm, title, description)
    }

    /**
     * Update pre-alarms
     */
    fun updatePreAlarms(preAlarms: ArrayList<PreAlarm>, title: String) {
        deletePreAlarms(preAlarms)

        for (alarm in preAlarms) {
            if (alarm.timeInMillis > System.currentTimeMillis())
                setExactAlarm(alarm.timeInMillis, alarm.idPreAlarm, PRE_ALARM_TITLE_NOTIFICATION, title)
        }
    }

    /**
     * Update alarm and pre-alarms
     */
    fun updateAlarmAndPreAlarms(idAlarm: Int, timeInMillis: Long, title: String, description: String, preAlarms: ArrayList<PreAlarm>) {
        updateAlarm(idAlarm, timeInMillis, title, description)
        updatePreAlarms(preAlarms, title)
    }

    private fun getIntent() = Intent(context, AlarmReceiver::class.java)

    private fun getPendingIntent(intent: Intent, idAlarm: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            idAlarm,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}