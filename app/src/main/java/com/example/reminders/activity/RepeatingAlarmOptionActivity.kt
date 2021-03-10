package com.example.reminders.activity

import android.content.Intent
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.keyIterator
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm
import com.example.reminders.constants.ConstantsAlarm.WEEK_DAY_OPTION
import com.example.reminders.constants.ConstantsTime.WEEK

/**
 * I will change this to an alert dialog in the future
 */
class RepeatingAlarmOptionActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton
    private lateinit var textRepeatingAlarmOption: String

    private lateinit var weekListView: ListView
    private lateinit var weekDaysArrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repeating_alarm_option)

        // radio group
        radioGroup = findViewById(R.id.radioGroupRepeatingAlarm)

        // Confirm button
        val confirmButton = findViewById<Button>(R.id.confirmRepeatingAlarmButton)
        confirmButton.setOnClickListener{
            checkButton()
        }

        // Spinner for intervals of time (hour, day, week, month, year)
        val spinner = findViewById<Spinner>(R.id.spinnerRepeatingAlarm)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this,
            R.array.repeatingAlarmOptionIntervals,
            android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this


        // Week days (M, T, W, T, F, S, S)
        weekListView = findViewById(R.id.week_list_view)
        weekDaysArrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_multiple_choice,
        resources.getStringArray(R.array.weekDays))
        weekListView.adapter = weekDaysArrayAdapter
        weekListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        weekListView
    }

    private fun checkButton() {
        val radioButtonId = radioGroup.checkedRadioButtonId

        radioButton = findViewById(radioButtonId)
        // Repeating option
        var option = radioButton.text.toString()
        // Days of the week if it is the case
        var weekOption = BooleanArray(7)

        // Check if it is the custom radio button
        if (radioButtonId == R.id.radioButtonCustom) {
            val repeatingValueOption = findViewById<EditText>(R.id.editTextNumberRepeatingAlarmValue).text.toString()
            option = "$repeatingValueOption $textRepeatingAlarmOption"

            // Check if it is the week interval
            if (textRepeatingAlarmOption.contains(WEEK))
                weekOption = getCheckedDays(weekListView.checkedItemPositions)
        }

        val resultIntent = Intent()
        resultIntent.putExtra(ConstantsAlarm.REPEATING_ALARM_OPTION, option)
        resultIntent.putExtra(WEEK_DAY_OPTION, weekOption)

        setResult(RESULT_OK, resultIntent)

        finish()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        textRepeatingAlarmOption = parent?.getItemAtPosition(position).toString()
        val array = resources.getStringArray(R.array.repeatingAlarmOptionIntervals)

        // If week -> make week list visible
        if (textRepeatingAlarmOption == array[2])
            weekListView.visibility = View.VISIBLE
        else
            weekListView.visibility = View.INVISIBLE
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    /**
     * Return a boolean array of checked days
     */
    private fun getCheckedDays(checkedItemPositions: SparseBooleanArray): BooleanArray {
        val option = BooleanArray(7)
        for (item in checkedItemPositions.keyIterator())
            if (checkedItemPositions[item])
                option[item] = true

        return option
    }
}