package com.example.reminders.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.reminders.R
import com.example.reminders.activity.PreAlarmOptionActivity
import com.example.reminders.activity.RepeatingAlarmOptionActivity
import com.example.reminders.constants.ConstantsAlarm
import com.example.reminders.constants.ConstantsDatabase
import com.example.reminders.constants.ConstantsNotification
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_PRE_ALARM
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_REPEATING_ALARM
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.data.RepeatingDetails
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil
import com.example.reminders.util.TAG
import com.example.reminders.util.TimeUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_reminder.*


class AddReminderFragment : Fragment() {
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
    val repeatingDetails = RepeatingDetails()
    lateinit var thisContext: Context

    lateinit var repeatingAlarmText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmService = AlarmService(thisContext)

        val addButton: Button
        val setAlarmButton: Button
        val preAlarmAddButton: FloatingActionButton

//        title = "Add reminder"
        view.apply {
            editTextTitle = findViewById(R.id.edit_title_reminder)
            editTextNotes = findViewById(R.id.edit_notes_reminder)
            addButton = findViewById(R.id.save_button)
            setAlarmButton = findViewById(R.id.setTimeButton)
            preAlarmAddButton = findViewById(R.id.pre_alarm_add_button)
            repeatingAlarmText = findViewById(R.id.repeatingAlarmText)
        }

        addButton.setOnClickListener(AddButtonClick())

        setAlarmButton.setOnClickListener{
            setAlarm()
        }

        preAlarmAddButton.setOnClickListener{
            addPreAlarmField()
        }

        repeatingAlarmText.setOnClickListener {
            val intent = Intent(thisContext, RepeatingAlarmOptionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_REPEATING_ALARM)
        }

        //setRepetitive.setOnClickListener { setAlarm { alarmService.setRepetitiveAlarm(it) } }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisContext = container?.context!!

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_reminder, container, false)
    }

    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            val title = editTextTitle.text.toString()
            val notes = editTextNotes.text.toString()

            if (title.trim().isEmpty() || notes.trim().isEmpty()) {
                Toast.makeText(v?.context, "Please insert a title and description", Toast.LENGTH_SHORT).show()
                return
            }

            val idAlarm = RandomUtil.getRandomInt()

            alarmService.setExactAlarm(timeInMillisReminder, idAlarm, title, notes)

            setPreAlarms(title)

            val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection(
                ConstantsDatabase.COLLECTION_REMINDERS
            )
            val reminder = Reminder(title, notes, idAlarm, yearReminder, monthReminder, dayReminder,
                hourReminder, minuteReminder, timeInMillisReminder)
            reminder.preAlarms = arrayPreAlerts
            if (repeatingDetails.isRepeating) {
                repeatingDetails.originalDay = dayReminder
                reminder.repeatingDetails = repeatingDetails
            }

            collectionReference.add(reminder)

            findNavController().navigate(
                AddReminderFragmentDirections.actionAddReminderFragmentToHomeFragment()
            )
        }
    }

    private fun setPreAlarms(title: String) {
        for (alarm in arrayPreAlerts) {
            alarm.timeInMillis = timeInMillisReminder - alarm.timeInMillis

            alarmService.setExactAlarm(alarm.timeInMillis, alarm.idPreAlarm,
                ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION, title)
        }
    }

    private fun setAlarm() {
        Calendar.getInstance().apply {
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
            DatePickerDialog(
                thisContext,
                0,
                { _, year, month, day ->
                    this.set(Calendar.YEAR, year)
                    this.set(Calendar.MONTH, month)
                    this.set(Calendar.DAY_OF_MONTH, day)

                    yearReminder = year
                    monthReminder = month
                    dayReminder = day

                    TimePickerDialog(
                        thisContext,
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
        val preAlertField = TextView(thisContext)
        preAlertField.setText(R.string.pre_alert_default_option)
        preAlertField.textSize = 18f
        rootPreAlarms.addView(preAlertField)

        preAlertField.setOnClickListener {
            val intent = Intent(thisContext, PreAlarmOptionActivity::class.java)
            preAlarmFieldReminder = preAlertField
            startActivityForResult(intent, REQUEST_CODE_PRE_ALARM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PRE_ALARM) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val option = data?.getStringExtra(ConstantsAlarm.PRE_ALARM_OPTION)

                preAlarmFieldReminder.text = option

                val idPreAlarm = RandomUtil.getRandomInt()
                val timeInMillis = TimeUtil.computeTimeInMillis(option)
                arrayPreAlerts.add(PreAlarm(timeInMillis, idPreAlarm,
                    TimeUtil.getValue(option),
                    TimeUtil.getTimeUnit(option)
                ))
            }
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.d(TAG, "Pre alarm canceled")
            }
        } else if (requestCode == REQUEST_CODE_REPEATING_ALARM) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val option = data?.getStringExtra(ConstantsAlarm.REPEATING_ALARM_OPTION)

                repeatingAlarmText.text = option

                Log.d(TAG, "$option")

                if (option != getString(R.string.repeating_alarm_default_option)) {
                    repeatingDetails.isRepeating = true
                    repeatingDetails.interval = TimeUtil.getValue(option).toInt()
                    repeatingDetails.intervalUnit = TimeUtil.getIntervalUnit(option)
                }
            }
        }
    }
}