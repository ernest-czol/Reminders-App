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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.reminders.R
import com.example.reminders.activity.PreAlarmOptionActivity
import com.example.reminders.activity.RepeatingAlarmOptionActivity
import com.example.reminders.constants.ConstantsAlarm
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_PRE_ALARM
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_REPEATING_ALARM
import com.example.reminders.data.PreAlarm
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.example.reminders.util.RandomUtil
import com.example.reminders.util.TAG
import com.example.reminders.util.TimeUtil
import com.example.reminders.viewModel.ReminderViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_edit_reminder.*


class AddReminderFragment : Fragment() {
    lateinit var editTextTitle: EditText
    lateinit var editTextNotes: EditText
    lateinit var preAlarmFieldReminder: TextView
    lateinit var repeatingAlarmText: TextView

    lateinit var thisContext: Context
    private val reminderViewModel: ReminderViewModel by activityViewModels()
    private val reminder = Reminder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val saveButton: Button
        val setTimeForAlarmButton: Button
        val preAlarmAddButton: FloatingActionButton

        view.apply {
            editTextTitle = findViewById(R.id.edit_title_reminder)
            editTextNotes = findViewById(R.id.edit_notes_reminder)
            saveButton = findViewById(R.id.save_button)
            setTimeForAlarmButton = findViewById(R.id.setTimeButton)
            preAlarmAddButton = findViewById(R.id.pre_alarm_add_button)
            repeatingAlarmText = findViewById(R.id.repeatingAlarmText)
        }

        saveButton.setOnClickListener(AddButtonClick())

        // Set date and time for this reminder
        setTimeForAlarmButton.setOnClickListener {
            setTimePickerDialog()
        }

        // Add a pre-alarm for this reminder
        preAlarmAddButton.setOnClickListener {
            addPreAlarmField()
        }

        // Set repeating option for this reminder
        repeatingAlarmText.setOnClickListener {
            val intent = Intent(thisContext, RepeatingAlarmOptionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_REPEATING_ALARM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //get context, needed for view model -> passed to alarm service
        thisContext = container?.context!!

        return inflater.inflate(R.layout.fragment_edit_reminder, container, false)
    }

    /**
     * Save button
     */
    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            // Validate the fields
            if (!validateFields(v))
                return

            // Generate random id for the main alarm
            reminder.idAlarm = RandomUtil.getRandomInt()

            // Construct the Reminder
            reminder.title = editTextTitle.text.toString()
            reminder.notes = editTextNotes.text.toString()

            // Save the original day (field day might change if day is last of month)
            reminder.repeatingDetails.originalDay = reminder.day

            // Set the alarms for this reminder
            reminderViewModel.setAlarms(AlarmService(thisContext), reminder)

            // Persist the reminder to db
            reminderViewModel.addReminder(reminder)

            // Navigate to HomeFragment
            findNavController().navigate(
                AddReminderFragmentDirections.actionAddReminderFragmentToHomeFragment()
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
     * Add a pre-alarm field to the view
     */
    private fun addPreAlarmField() {
        val preAlertField = TextView(thisContext)
        preAlertField.setText(R.string.pre_alert_default_option)
        preAlertField.textSize = 18f
        rootPreAlarms.addView(preAlertField)

        // Change to an alert dialog in the future
        preAlertField.setOnClickListener {
            val intent = Intent(thisContext, PreAlarmOptionActivity::class.java)
            preAlarmFieldReminder = preAlertField
            startActivityForResult(intent, REQUEST_CODE_PRE_ALARM)
        }
    }

    /**
     * Used to get options for pre-alarm or repeating details
     * Use in the future an alert dialog for this
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Add a pre-alarm
        if (requestCode == REQUEST_CODE_PRE_ALARM) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val option = data?.getStringExtra(ConstantsAlarm.PRE_ALARM_OPTION)

                preAlarmFieldReminder.text = option

                val idPreAlarm = RandomUtil.getRandomInt()
                val timeInMillis = TimeUtil.computeTimeInMillis(option)
                reminder.preAlarms.add(
                    PreAlarm(
                        timeInMillis, idPreAlarm,
                        TimeUtil.getValue(option),
                        TimeUtil.getTimeUnit(option)
                    )
                )
            }
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                Log.d(TAG, "Pre alarm canceled")
            }
        } else // Add a repeating details for this reminder
            if (requestCode == REQUEST_CODE_REPEATING_ALARM) {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val option = data?.getStringExtra(ConstantsAlarm.REPEATING_ALARM_OPTION)

                    repeatingAlarmText.text = option

                    Log.d(TAG, "$option")

                    if (option != getString(R.string.repeating_alarm_default_option)) {
                        reminder.repeatingDetails.isRepeating = true
                        reminder.repeatingDetails.interval = TimeUtil.getValue(option).toInt()
                        reminder.repeatingDetails.intervalUnit = TimeUtil.getIntervalUnit(option)
                    }
                }
            }
    }

    /**
     * Show a date and time picker dialog for the user to set the alarm
     */
    private fun setTimePickerDialog() {
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