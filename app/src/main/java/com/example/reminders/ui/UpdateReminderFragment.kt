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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.reminders.R
import com.example.reminders.activity.PreAlarmOptionActivity
import com.example.reminders.constants.ConstantsAlarm
import com.example.reminders.constants.ConstantsAlarm.PRE_ALARM_OPTION
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil.getRandomInt
import com.example.reminders.util.TAG
import com.example.reminders.util.TimeUtil.computeTimeInMillis
import com.example.reminders.util.TimeUtil.getTimeUnit
import com.example.reminders.util.TimeUtil.getValue
import com.example.reminders.viewModel.ReminderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_edit_reminder.*
import java.util.concurrent.TimeUnit

class UpdateReminderFragment : Fragment() {
    lateinit var editTextTitle: EditText
    lateinit var editTextNotes: EditText
    lateinit var preAlertFieldReminder: TextView

    var idPreAlarm_r: Int = 0

    lateinit var thisContext: Context
    private val args: UpdateReminderFragmentArgs by navArgs()
    private val viewModel: ReminderViewModel by activityViewModels()
    private val reminder = Reminder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Get the reminder id from navigation arguments
        args.idReminder?.let { reminder.id = it }

        val saveButton: Button
        val setTimeForAlarmButton: Button
        val preAlarmAddButton: FloatingActionButton

        view.apply {
            editTextTitle = findViewById(R.id.edit_title_reminder)
            editTextNotes = findViewById(R.id.edit_notes_reminder)

            saveButton = findViewById(R.id.save_button)
            setTimeForAlarmButton = findViewById(R.id.setTimeButton)
            preAlarmAddButton = findViewById(R.id.pre_alarm_add_button)
        }

        setValuesForReminder()

        // Save button click
        saveButton.setOnClickListener(UpdateButtonClick())

        // Set time for main alarms
        setTimeForAlarmButton.setOnClickListener {
            setTimeForAlarm()
        }

        // Add a new pre-alarm
        preAlarmAddButton.setOnClickListener {
            addPreAlarmField()
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

    /**
     * Get current reminder values
     */
    private fun setValuesForReminder() {
        viewModel.getReminder(reminder.id).observe(viewLifecycleOwner, Observer {
            it?.let {
                reminder.title = it.title
                reminder.notes = it.notes

                reminder.year = it.year
                reminder.month = it.month
                reminder.day = it.day
                reminder.hour = it.hour
                reminder.minute = it.minute

                reminder.timeInMillis = it.timeInMillis

                reminder.idAlarm = it.idAlarm

                reminder.preAlarms.addAll(it.preAlarms)

                updateUI()
            }
        })
    }

    /**
     * Update UI values
     * More in the future
     */
    private fun updateUI() {
        // Title and notes
        editTextTitle.setText(reminder.title)
        editTextNotes.setText(reminder.notes)

        // Pre-alarm fields
        for (preAlarm in reminder.preAlarms)
            addPreAlarmField("${preAlarm.valueTimeUnit} ${preAlarm.timeUnit}", preAlarm.idPreAlarm)
    }

    /**
     * Add a pre alarm field to the view
     */
    private fun addPreAlarmField(value: String = ConstantsAlarm.NO_PRE_ALARM, idPreAlarm: Int = 0) {
        val preAlarmField = TextView(thisContext)
        preAlarmField.text = value
        preAlarmField.textSize = 18f
        rootPreAlarms.addView(preAlarmField)

        // I will change to an alert dialog in the future
        preAlarmField.setOnClickListener {
            preAlertFieldReminder = preAlarmField
            idPreAlarm_r = idPreAlarm
            startActivityForResult(Intent(thisContext, PreAlarmOptionActivity::class.java), 1)
        }
    }

    /**
     * Save button
     */
    inner class UpdateButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            // Validate the fields
            if (!validateFields(v))
                return

            // Update title and notes
            reminder.title = editTextTitle.text.toString()
            reminder.notes = editTextNotes.text.toString()

            // Update alarms
            viewModel.updateAlarms(AlarmService(thisContext), reminder)
            // Persist reminder to db
            viewModel.updateReminder(reminder)

            // Navigate back to HomeFragment
            findNavController().navigate(
                UpdateReminderFragmentDirections.actionUpdateReminderFragmentToHomeFragment()
            )
        }

        /**
         * Check if the fields are empty
         * More in the future
         */
        private fun validateFields(v: View?): Boolean {
            val title = editTextTitle.text.toString()
            val notes = editTextNotes.text.toString()

            if (title.trim().isEmpty() || notes.trim().isEmpty()) {
                Toast.makeText(
                    v?.context,
                    "Please insert a title and description",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            return true
        }
    }

    /**
     * Update pre-alarms values
     */
    private fun updatePreAlarmValues(
        idPreAlarm: Int,
        timeInMillis: Long,
        value: Long,
        timeUnit: TimeUnit
    ) {
        // Update pre-alarm in the array if already exists
        reminder.preAlarms.find { (it.idPreAlarm == idPreAlarm) }.apply {
            this?.timeInMillis = reminder.timeInMillis - timeInMillis
            this?.timeUnit = timeUnit
            this?.valueTimeUnit = value

            this?.let{return}
        }

        // Add new pre-alarm
        reminder.preAlarms.add(
            PreAlarm(
                reminder.timeInMillis - timeInMillis,
                idPreAlarm,
                value,
                timeUnit
            )
        )
    }

    /**
     * Used to get options for pre-alarm or repeating details
     * I will use in the future an alert dialog for this
     */
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

                updatePreAlarmValues(
                    idPreAlert,
                    timeInMillis,
                    getValue(option),
                    getTimeUnit(option)
                )
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "pre alert canceled")
            }
        }
    }

    /**
     * Set date and time for an alarm
     */
    private fun setTimeForAlarm() {
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

                    reminder.year = year
                    reminder.month = month
                    reminder.day = day

                    TimePickerDialog(
                        thisContext,
                        0,
                        { _, hour, minute ->
                            this.set(Calendar.HOUR_OF_DAY, hour)
                            this.set(Calendar.MINUTE, minute)

                            reminder.hour = hour
                            reminder.minute = minute

                            reminder.timeInMillis = this.timeInMillis
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
}