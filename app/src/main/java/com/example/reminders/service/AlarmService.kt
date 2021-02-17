package com.example.reminders.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_EXACT
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_REPETITIVE
import com.example.reminders.constants.ConstantsAlarm.EXACT_ALARM_TIME
import com.example.reminders.constants.ConstantsAlarm.ID_ALARM
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsReminder.DESCRIPTION_REMINDER
import com.example.reminders.constants.ConstantsReminder.TITLE_REMINDER
import com.example.reminders.data.PreAlarm
import com.example.reminders.receiver.AlarmReceiver

class AlarmService(private val context: Context) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

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

    fun setRepetitiveAlarm(timeInMillis: Long) {
        setAlarm(
            timeInMillis,
            getPendingIntent(
                getIntent().apply {
                    action = ACTION_SET_REPETITIVE
                    putExtra(EXACT_ALARM_TIME, timeInMillis)
                },
                0
            )
        )
    }

    private fun setAlarm(timeInMillis: Long, pendingIntent: PendingIntent) {
        alarmManager?.let {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    fun deleteAlarm(idAlarm: Int) {
        val intent = getIntent().apply {
            action = ACTION_SET_EXACT
        }

        alarmManager?.cancel(getPendingIntent(intent, idAlarm))
    }

    private fun deletePreAlarms(preAlarms: ArrayList<PreAlarm>) {
        val intent = getIntent().apply {
            action = ACTION_SET_EXACT
        }

//        val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection("reminders")
//
//        collectionReference.document(idReminder).get().addOnCompleteListener{
//            val doc = it.result
//            val preAlarms = doc?.get("preAlarms")
//        }

        for (preAlarm in preAlarms) {
            alarmManager?.cancel(getPendingIntent(intent, preAlarm.idPreAlarm))
        }
    }

    fun deleteAlarmAndPreAlarms(idAlarm: Int?, preAlarms: ArrayList<PreAlarm>?) {
        idAlarm?.let{deleteAlarm(it)}
        preAlarms?.let{deletePreAlarms(it)}
    }

    fun updateAlarm(idAlarm: Int, timeInMillis: Long, title: String, description: String) {
        deleteAlarm(idAlarm)

        if (timeInMillis > System.currentTimeMillis())
            setExactAlarm(timeInMillis, idAlarm, title, description)
    }

    fun updatePreAlarms(preAlarms: ArrayList<PreAlarm>, title: String) {
        deletePreAlarms(preAlarms)

        for (alarm in preAlarms) {
            if (alarm.timeInMillis > System.currentTimeMillis())
                setExactAlarm(alarm.timeInMillis, alarm.idPreAlarm, PRE_ALARM_TITLE_NOTIFICATION, title)
        }
    }

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