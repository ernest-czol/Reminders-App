package com.example.reminders.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm.PRE_ALARM_OPTION
import com.example.reminders.constants.ConstantsDatabase.COLLECTION_REMINDERS
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil.getRandomInt
import com.example.reminders.util.TimeUtil.computeTimeInMillis
import com.example.reminders.util.TimeUtil.getTimeUnit
import com.example.reminders.util.TimeUtil.getValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_edit_reminder.*

private const val TAG = "NewReminder"

class NewReminderActivity : AppCompatActivity() {
    lateinit var editTextTitle: EditText
    lateinit var editTextNotes: EditText
    lateinit var alarmService: AlarmService

    var dayReminder: Int = 0
    var monthReminder: Int = 0
    var yearReminder: Int = 0
    var hourReminder: Int = 0
    var minuteReminder: Int = 0
    var timeInMillisReminder: Long = 0

    lateinit var preAlarmFieldReminder: TextView
    val arrayPreAlerts = ArrayList<PreAlarm>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reminder)

        alarmService = AlarmService(this)

        title = "Add reminder"

        editTextTitle = findViewById(R.id.edit_title_reminder)
        editTextNotes = findViewById(R.id.edit_notes_reminder)

        val addButton = findViewById<Button>(R.id.save_button)
        addButton.setOnClickListener(AddButtonClick())

        val setAlarmButton = findViewById<Button>(R.id.setTimeButton)
        setAlarmButton.setOnClickListener{
            setAlarm()
        }

        val preAlarmAddButton = findViewById<FloatingActionButton>(R.id.pre_alarm_add_button)
        preAlarmAddButton.setOnClickListener{
            addPreAlarmField()
        }

        //setRepetitive.setOnClickListener { setAlarm { alarmService.setRepetitiveAlarm(it) } }
    }

    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            val title = editTextTitle.text.toString()
            val notes = editTextNotes.text.toString()

            if (title.trim().isEmpty() || notes.trim().isEmpty()) {
                Toast.makeText(v?.context, "Please insert a title and description", Toast.LENGTH_SHORT).show()
                return
            }

            val idAlarm = getRandomInt()

            alarmService.setExactAlarm(timeInMillisReminder, idAlarm, title, notes)

            setPreAlarms(title)

            val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection(COLLECTION_REMINDERS)
            val reminder = Reminder(title, notes, idAlarm, yearReminder, monthReminder, dayReminder,
                hourReminder, minuteReminder, timeInMillisReminder)
            reminder.preAlarms = arrayPreAlerts
            collectionReference.add(reminder)

            finish()
        }
    }

    private fun setPreAlarms(title: String) {
        for (alarm in arrayPreAlerts) {
            alarm.timeInMillis = timeInMillisReminder - alarm.timeInMillis

            alarmService.setExactAlarm(alarm.timeInMillis, alarm.idPreAlarm, PRE_ALARM_TITLE_NOTIFICATION, title)
        }
    }

    private fun setAlarm() {
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            DatePickerDialog(
                this@NewReminderActivity,
                0,
                { _, year, month, day ->
                    this.set(Calendar.YEAR, year)
                    this.set(Calendar.MONTH, month)
                    this.set(Calendar.DAY_OF_MONTH, day)

                    yearReminder = year
                    monthReminder = month
                    dayReminder = day

                    TimePickerDialog(
                        this@NewReminderActivity,
                        0,
                        { _, hour, minute ->
                            this.set(Calendar.HOUR_OF_DAY, hour)
                            this.set(Calendar.MINUTE, minute)

                            hourReminder = hour
                            minuteReminder = minute

                            timeInMillisReminder = this.timeInMillis
                        },
                        this.get(Calendar.HOUR_OF_DAY),
                        this.get(Calendar.MINUTE),
                        false
                    ).show()
                },
                this.get(Calendar.YEAR),
                this.get(Calendar.MONTH),
                this.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun addPreAlarmField() {
        val preAlertField = TextView(this)
        preAlertField.setText(R.string.pre_alert_default_option)
        preAlertField.textSize = 18f
        rootPreAlarms.addView(preAlertField)

        preAlertField.setOnClickListener {
            val intent = Intent(this, PreAlertOptionActivity::class.java)
            preAlarmFieldReminder = preAlertField
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val option = data?.getStringExtra(PRE_ALARM_OPTION)

                preAlarmFieldReminder.text = option

                val idPreAlarm = getRandomInt()
                val timeInMillis = computeTimeInMillis(option)
                arrayPreAlerts.add(PreAlarm(timeInMillis, idPreAlarm, getValue(option), getTimeUnit(option)))
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "Pre alarm canceled")
            }
        }
    }
}