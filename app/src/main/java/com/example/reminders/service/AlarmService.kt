package com.example.reminders.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_MAIN_ALARM
import com.example.reminders.constants.ConstantsAlarm.ACTION_SIMPLE_ALARM
import com.example.reminders.constants.ConstantsAlarm.DESCRIPTION_ALARM
import com.example.reminders.constants.ConstantsAlarm.TITLE_ALARM
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsReminder.ID_REMINDER
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.model.Repository
import com.example.reminders.receiver.AlarmReceiver
import com.example.reminders.util.IntervalUnit
import com.example.reminders.util.TimeUtil.computeDay
import com.example.reminders.util.TimeUtil.computeMonth
import com.example.reminders.util.TimeUtil.computeYear
import com.example.reminders.util.TimeUtil.convertDate
import com.example.reminders.util.TimeUtil.getDay
import com.example.reminders.util.TimeUtil.getHour
import com.example.reminders.util.TimeUtil.getMonth
import com.example.reminders.util.TimeUtil.getYear
import java.util.*
import java.util.concurrent.TimeUnit

class AlarmService(private val context: Context) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

    /**
     * Set main alarm for a reminder
     */
    private fun setMainAlarm(reminder: Reminder) {
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
    private fun setSimpleAlarm(
        timeInMillis: Long,
        idAlarm: Int,
        title: String,
        description: String
    ) {
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

    /**
     * Get intent
     */
    private fun getIntent() = Intent(context, AlarmReceiver::class.java)

    /**
     * Get pending intent
     */
    private fun getPendingIntent(intent: Intent, idAlarm: Int): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            idAlarm,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Set repeating alarm
     */
    fun setRepeatingAlarm(reminder: Reminder) {
        val intervalUnit = reminder.repeatingDetails.intervalUnit
        val intervalValue = reminder.repeatingDetails.interval

        // Interval unit cases (hour, day, week, month, year)
        when (intervalUnit) {
            IntervalUnit.HOUR -> repeatReminderByHour(reminder, intervalValue)
            IntervalUnit.DAY -> repeatReminderByDay(reminder, intervalValue)
            IntervalUnit.WEEK -> repeatReminderByWeek(reminder, intervalValue)
            IntervalUnit.MONTH -> repeatReminderByMonth(reminder, intervalValue)
            IntervalUnit.YEAR -> repeatReminderByYear(reminder, intervalValue)
        }

        // Update the reminder and set the new alarms
        Repository.updateReminder(reminder)
        setAlarms(reminder)
    }

    /**
     * Repeat reminder by hours
     */
    private fun repeatReminderByHour(reminder: Reminder, intervalValue: Long) {
        // New time in milliseconds
        reminder.timeInMillis += TimeUnit.HOURS.toMillis(intervalValue)
        // Convert milliseconds to date
        val dateFormatted = convertDate(reminder.timeInMillis)

        // Update reminder`s fields
        reminder.hour = getHour(dateFormatted)
        reminder.day = getDay(dateFormatted)
        reminder.month = getMonth(dateFormatted)
        reminder.year = getYear(dateFormatted)
    }

    /**
     * Repeat reminder by days
     */
    private fun repeatReminderByDay(reminder: Reminder, intervalValue: Long) {
        // New time in milliseconds
        reminder.timeInMillis += TimeUnit.DAYS.toMillis(intervalValue)
        // Convert milliseconds to date
        val dateFormatted = convertDate(reminder.timeInMillis)

        // Update reminder`s fields
        reminder.day = getDay(dateFormatted)
        reminder.month = getMonth(dateFormatted)
        reminder.year = getYear(dateFormatted)
    }

    /**
     * Repeat reminder by weeks
     */
    private fun repeatReminderByWeek(reminder: Reminder, intervalValue: Long) {
        // current day, first day, last day and next day
        val currentDayIndex = reminder.repeatingDetails.weekOptions.currentDayIndex
        val firstDayIndex = reminder.repeatingDetails.weekOptions.firstDayIndex
        val lastDayIndex = reminder.repeatingDetails.weekOptions.lastDayIndex
        val nextDayIndex = reminder.repeatingDetails.weekOptions.getNextDayIndex()

        // If last day in the week move to the first day
        if (currentDayIndex != lastDayIndex)
            reminder.timeInMillis += TimeUnit.DAYS.toMillis((nextDayIndex - currentDayIndex).toLong())
        else
            reminder.timeInMillis += TimeUnit.DAYS.toMillis(7 * intervalValue - (lastDayIndex - firstDayIndex))

        // Convert milliseconds to date
        val dateFormatted = convertDate(reminder.timeInMillis)

        // Update reminder`s fields
        reminder.repeatingDetails.weekOptions.currentDayIndex = nextDayIndex
        reminder.day = getDay(dateFormatted)
        reminder.month = getMonth(dateFormatted)
        reminder.year = getYear(dateFormatted)
    }

    /**
     * Repeat reminder by months
     */
    private fun repeatReminderByMonth(reminder: Reminder, intervalValue: Long) {
        // year, month, day
        val year = computeYear(intervalValue.toInt(), reminder.month, reminder.year)
        val month = computeMonth(intervalValue.toInt(), reminder.month)
        val day = computeDay(
            intervalValue.toInt(),
            reminder.month,
            reminder.year,
            reminder.repeatingDetails.originalDay
        )

        // Calendar instance
        val cal = Calendar.getInstance().apply {
            // Month starts from 0
            set(year, month - 1, day, reminder.hour, reminder.minute)
        }
        // Update reminder`s fields
        reminder.timeInMillis = cal.timeInMillis

        reminder.day = day
        reminder.month = month
        reminder.year = year
    }

    /**
     * Repeat reminder by years
     */
    private fun repeatReminderByYear(reminder: Reminder, intervalValue: Long) {
        // year, day
        val year = reminder.year + intervalValue.toInt()
        val day = computeDay(reminder.repeatingDetails.originalDay, reminder.month, year)

        // Calendar instance
        val cal = Calendar.getInstance().apply {
            set(
                year,
                reminder.month - 1, // starts from 0
                day,
                reminder.hour,
                reminder.minute
            )
        }
        // Update reminder fields
        reminder.timeInMillis = cal.timeInMillis

        reminder.day = day
        reminder.year = year
    }

    /**
     * Delete an alarm
     */
    fun deleteAlarm(idAlarm: Int) {
        val intent = getIntent().apply {
            action = ACTION_SET_MAIN_ALARM
        }

        alarmManager?.cancel(getPendingIntent(intent, idAlarm))
    }

    /**
     * Delete pre-alarms
     */
    private fun deletePreAlarms(preAlarms: ArrayList<PreAlarm>) {
        val intent = getIntent().apply {
            action = ACTION_SIMPLE_ALARM
        }

        for (preAlarm in preAlarms) {
            alarmManager?.cancel(getPendingIntent(intent, preAlarm.idPreAlarm))
        }
    }

    /**
     * Delete alarm and pre-alarms
     */
    fun deleteAlarmAndPreAlarms(idAlarm: Int?, preAlarms: ArrayList<PreAlarm>?) {
        idAlarm?.let { deleteAlarm(it) }
        preAlarms?.let { deletePreAlarms(it) }
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
                setSimpleAlarm(
                    preAlarm.timeInMillis,
                    preAlarm.idPreAlarm,
                    PRE_ALARM_TITLE_NOTIFICATION,
                    reminder.title
                )
        }
    }

    /**
     * Update alarm and pre-alarms
     */
    fun updateAlarmAndPreAlarms(reminder: Reminder) {
        updateAlarm(reminder)
        updatePreAlarms(reminder)
    }

    /**
     * Set pre-alarms for a reminder
     */
    private fun setPreAlarms(reminder: Reminder) {
        for (preAlarm in reminder.preAlarms) {
            preAlarm.timeInMillis = reminder.timeInMillis - preAlarm.timeInMillis

            setSimpleAlarm(
                preAlarm.timeInMillis,
                preAlarm.idPreAlarm,
                PRE_ALARM_TITLE_NOTIFICATION,
                reminder.title
            )
        }
    }

    /**
     * Set alarms for a reminder
     */
    fun setAlarms(reminder: Reminder) {
        setMainAlarm(reminder)
        setPreAlarms(reminder)
    }
}