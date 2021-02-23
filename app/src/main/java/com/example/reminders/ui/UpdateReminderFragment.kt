package com.example.reminders.ui

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reminders.R
import com.example.reminders.activity.PreAlarmOptionActivity
import com.example.reminders.constants.ConstantsAlarm
import com.example.reminders.constants.ConstantsAlarm.PRE_ALARM_OPTION
import com.example.reminders.constants.ConstantsDatabase
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil.getRandomInt
import com.example.reminders.util.TimeUtil.computeTimeInMillis
import com.example.reminders.util.TimeUtil.getTimeUnit
import com.example.reminders.util.TimeUtil.getValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_reminder.*
import java.util.concurrent.TimeUnit

class UpdateReminderFragment : Fragment() {
    lateinit var editTextTitle: EditText
    lateinit var editTextNotes: EditText
    lateinit var idReminder: String
    lateinit var docReference: DocumentReference
    lateinit var alarmService: AlarmService

    var dayReminder: Int = 0
    var monthReminder: Int = 0
    var yearReminder: Int = 0
    var hourReminder: Int = 0
    var minuteReminder: Int = 0
    var timeInMillisReminder: Long = 0

    var idAlarm: Int = 0
    lateinit var preAlertFieldReminder: TextView
    val arrayPreAlerts = ArrayList<PreAlarm>()
    var idPreAlarm_r: Int = 0

    lateinit var thisContext: Context

    private val args: UpdateReminderFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        alarmService = AlarmService(thisContext)

        args.idReminder?.let{idReminder = it}

        val addButton: Button
        val setAlarmButton: Button
        val preAlertAddButton: FloatingActionButton

        view.apply {
            editTextTitle = findViewById(R.id.edit_title_reminder)
            editTextNotes = findViewById(R.id.edit_notes_reminder)

            addButton = findViewById(R.id.save_button)
            setAlarmButton = findViewById(R.id.setTimeButton)
            preAlertAddButton = findViewById(R.id.pre_alarm_add_button)
        }

        setValues()

        addButton.setOnClickListener(UpdateButtonClick())

        setAlarmButton.setOnClickListener{
            setAlarm()
        }

        preAlertAddButton.setOnClickListener{
            addPreAlertField()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisContext = container?.context!!

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_reminder, container, false)
    }

    private fun setValues() {
        docReference = FirebaseFirestore.getInstance()
            .collection(ConstantsDatabase.COLLECTION_REMINDERS)
            .document(idReminder)

        docReference.get().addOnSuccessListener { documentSnapshot ->
            val reminder = documentSnapshot.toObject(Reminder::class.java)

            editTextTitle.setText(reminder!!.title)
            editTextNotes.setText(reminder.notes)

            yearReminder = reminder.year
            monthReminder = reminder.month
            dayReminder = reminder.day
            hourReminder = reminder.hour
            minuteReminder = reminder.minute

            timeInMillisReminder = reminder.timeInMillis

            idAlarm = reminder.idAlarm

            setValuesPreAlarms(reminder.preAlarms)
        }
    }

    private fun setValuesPreAlarms(preAlarms: ArrayList<PreAlarm>) {
        arrayPreAlerts.addAll(preAlarms)
        for (preAlarm in preAlarms)
            addPreAlertField("${preAlarm.valueTimeUnit} ${preAlarm.timeUnit}", preAlarm.idPreAlarm)
    }

    inner class UpdateButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            val title = editTextTitle.text.toString()
            val notes = editTextNotes.text.toString()

            if (title.trim().isEmpty() || notes.trim().isEmpty()) {
                Toast.makeText(v?.context, "Please insert a title and description", Toast.LENGTH_SHORT).show()
                return
            }

            alarmService.updateAlarmAndPreAlarms(idAlarm, timeInMillisReminder, title, notes, arrayPreAlerts)

            val reminder = Reminder(title, notes, idAlarm, yearReminder, monthReminder, dayReminder, hourReminder, minuteReminder, timeInMillisReminder)
            reminder.preAlarms = arrayPreAlerts

            docReference.set(reminder)

            findNavController().navigate(
                UpdateReminderFragmentDirections.actionUpdateReminderFragmentToHomeFragment()
            )
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

    private fun addPreAlertField(value: String = ConstantsAlarm.NO_PRE_ALARM, idPreAlarm: Int = 0) {
        val preAlertField = TextView(thisContext)
        preAlertField.text = value
        preAlertField.textSize = 18f
        rootPreAlarms.addView(preAlertField)

        preAlertField.setOnClickListener {
            preAlertFieldReminder = preAlertField
            idPreAlarm_r = idPreAlarm
            startActivityForResult(Intent(thisContext, PreAlarmOptionActivity::class.java), 1)
        }
    }

    private fun updatePreAlarmValues(idPreAlert: Int, timeInMillis: Long, value: Long, timeUnit: TimeUnit) {
        for (preAlarm in arrayPreAlerts)
            if (preAlarm.idPreAlarm == idPreAlert) {
                preAlarm.timeInMillis = timeInMillisReminder - timeInMillis
                preAlarm.timeUnit = timeUnit
                preAlarm.valueTimeUnit = value

                return
            }

        arrayPreAlerts.add(PreAlarm(timeInMillisReminder - timeInMillis, idPreAlert, value, timeUnit))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val option = data?.getStringExtra(PRE_ALARM_OPTION)

                preAlertFieldReminder.text = option

                val idPreAlert: Int = if (idPreAlarm_r == 0)
                    getRandomInt()
                else
                    idPreAlarm_r
                val timeInMillis = computeTimeInMillis(option)

                updatePreAlarmValues(idPreAlert, timeInMillis, getValue(option), getTimeUnit(option))
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d("NewRem", "pre alert canceled")
            }
        }
    }
}