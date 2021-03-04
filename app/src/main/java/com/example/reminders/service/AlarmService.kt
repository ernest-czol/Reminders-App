package com.example.reminders.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_EXACT
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_MAIN_ALARM
import com.example.reminders.constants.ConstantsAlarm.ACTION_SIMPLE_ALARM
import com.example.reminders.constants.ConstantsAlarm.DESCRIPTION_ALARM
import com.example.reminders.constants.ConstantsAlarm.TITLE_ALARM
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsReminder.ID_REMINDER
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
     * Set main alarm for a reminder
     */
    fun setMainAlarm(reminder: Reminder) {
        setAlarm(
            reminder.timeInMillis,
            getPendingIntent(
                getIntent().apply {
                    action = ACTION_SET_MAIN_ALARM
                    putExtra(ID_REMINDER, reminder.id)
                },
                reminder.idAlarm
            )
        )
    }

    /**
     * Set a simple alarm
     */
    fun setSimpleAlarm(timeInMillis: Long, idAlarm: Int, title: String, description: String) {
        setAlarm(
            timeInMillis,
            getPendingIntent(
                getIntent().apply {
                    action = ACTION_SIMPLE_ALARM
                    putExtra(TITLE_ALARM, title)
                    putExtra(DESCRIPTION_ALARM, description)
                },
                idAlarm
            )
        )
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

    private fun getIntent() = Intent(context, AlarmReceiver::class.java)

    private fun getPendingIntent(intent: Intent, idAlarm: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            idAlarm,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Set repeating alarm TODO
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

        setMainAlarm(reminder)
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
    fun updateAlarm(reminder: Reminder) {
        deleteAlarm(reminder.idAlarm)

        if (reminder.timeInMillis > System.currentTimeMillis())
            setMainAlarm(reminder)
    }

    /**
     * Update pre-alarms
     */
    fun updatePreAlarms(reminder: Reminder) {
        deletePreAlarms(reminder.preAlarms)

        for (preAlarm in reminder.preAlarms) {
            if (preAlarm.timeInMillis > System.currentTimeMillis())
                setSimpleAlarm(preAlarm.timeInMillis, preAlarm.idPreAlarm, PRE_ALARM_TITLE_NOTIFICATION, reminder.title)
        }
    }

    /**
     * Update alarm and pre-alarms
     */
    fun updateAlarmAndPreAlarms(reminder: Reminder) {
        updateAlarm(reminder)
        updatePreAlarms(reminder)
    }
}