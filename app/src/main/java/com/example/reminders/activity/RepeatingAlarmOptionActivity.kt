package com.example.reminders.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm

/**
 * I will change this to an alert dialog in the future
 */
class RepeatingAlarmOptionActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton
    private lateinit var textRepeatingAlarmOption: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repeating_alarm_option)

        radioGroup = findViewById(R.id.radioGroupRepeatingAlarm)

        val confirmButton = findViewById<Button>(R.id.confirmRepeatingAlarmButton)
        confirmButton.setOnClickListener{
            checkButton()
        }

        val spinner = findViewById<Spinner>(R.id.spinnerRepeatingAlarm)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this,
            R.array.repeatingAlarmOptionIntervals,
            android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
    }

    private fun checkButton() {
        val radioButtonId = radioGroup.checkedRadioButtonId

        radioButton = findViewById(radioButtonId)
        var option = radioButton.text.toString()

        if (radioButtonId == R.id.radioButtonCustom) {
            val preAlertValueOption = findViewById<EditText>(R.id.editTextNumberRepeatingAlarmValue).text.toString()
            option = "$preAlertValueOption $textRepeatingAlarmOption"
        }

        val resultIntent = Intent()
        resultIntent.putExtra(ConstantsAlarm.REPEATING_ALARM_OPTION, option)

        setResult(RESULT_OK, resultIntent)

        finish()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        textRepeatingAlarmOption = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}