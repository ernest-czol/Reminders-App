package com.example.reminders.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_EXACT
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_REPETITIVE
import com.example.reminders.constants.ConstantsAlarm.ID_ALARM
import com.example.reminders.constants.ConstantsAlarm.TIME_UNIT
import com.example.reminders.constants.ConstantsAlarm.TIME_UNIT_VALUE
import com.example.reminders.constants.ConstantsNotification.DONE
import com.example.reminders.constants.ConstantsNotification.ID_NOTIFICATION
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_CHANNEL_ID
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsNotification.SNOOZE
import com.example.reminders.constants.ConstantsReminder.DESCRIPTION_REMINDER
import com.example.reminders.constants.ConstantsReminder.TITLE_REMINDER
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_DONE
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_SNOOZE
import com.example.reminders.constants.ConstantsTime.DAY
import com.example.reminders.constants.ConstantsTime.HOUR
import com.example.reminders.constants.ConstantsTime.MINUTE
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil.getRandomInt
import io.karn.notify.internal.utils.Action
import java.util.*
import java.util.concurrent.TimeUnit


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val titleReminder = intent.getStringExtra(TITLE_REMINDER)
        val descriptionReminder = intent.getStringExtra(DESCRIPTION_REMINDER)
        val idAlarm = intent.getIntExtra(ID_ALARM, 0)

        val timeUnit: TimeUnit = when (intent.getStringExtra(TIME_UNIT)) {
            DAY -> TimeUnit.DAYS
            HOUR -> TimeUnit.HOURS
            MINUTE -> TimeUnit.MINUTES
            else -> TimeUnit.MILLISECONDS
        }

        val valueTimeUnit = intent.getLongExtra(TIME_UNIT_VALUE, 0)

        when (intent.action) {
            ACTION_SET_EXACT -> {
                sendNotification(context, titleReminder, descriptionReminder, idAlarm)
            }

            ACTION_SET_REPETITIVE -> {
                setRepetitiveAlarm(AlarmService(context), timeUnit, valueTimeUnit)
                //buildNotification(context, "Set Repetitive Exact Time", convertDate(timeInMillis))
            }
        }
    }

    private fun setRepetitiveAlarm(alarmService: AlarmService, timeUnit: TimeUnit, timeValue: Long) {
        val cal = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis + timeUnit.toMillis(timeValue)
            //Timber.d("Set alarm for next week same time - ${convertDate(this.timeInMillis)}")
        }
        alarmService.setRepetitiveAlarm(cal.timeInMillis)
    }

    private fun sendNotification(context: Context, textTitle: String?, textContent: String?, idAlarm: Int) {
        val snoozeIntent = createSnoozeIntent(context, textTitle, textContent, idAlarm)

        val pendingIntentSnooze = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SNOOZE,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val doneIntent = Intent(context, NotificationReceiver::class.java)
        doneIntent.action = DONE

        val pendingIntentDone = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DONE,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = buildNotification(context, textTitle, textContent, pendingIntentSnooze, pendingIntentDone)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            val notificationId = getRandomInt()
            snoozeIntent.putExtra(ID_NOTIFICATION, notificationId)

            notify(notificationId, builder.build())
        }
    }

    private fun buildNotification(context: Context, textTitle: String?, textContent: String?,
    pendingIntentSnooze: PendingIntent, pendingIntentDone: PendingIntent): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (textTitle != PRE_ALARM_TITLE_NOTIFICATION) {
            builder.addAction(Action(R.drawable.ic_app_icon, DONE, pendingIntentDone))
            builder.addAction(Action(R.drawable.ic_app_icon, SNOOZE, pendingIntentSnooze))
        }

        return builder
    }

    private fun createSnoozeIntent(context: Context, textTitle: String?, textContent: String?, idAlarm: Int): Intent {
        val snoozeIntent = Intent(context, NotificationReceiver::class.java)
        snoozeIntent.action = SNOOZE
        snoozeIntent.putExtra(TITLE_REMINDER, textTitle)
        snoozeIntent.putExtra(DESCRIPTION_REMINDER, textContent)
        snoozeIntent.putExtra(ID_ALARM, idAlarm)

        return snoozeIntent
    }
}