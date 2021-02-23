package com.example.reminders.activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm.PRE_ALARM_OPTION

class PreAlarmOptionActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButton: RadioButton
    private lateinit var textPreAlertOption: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_alarm_option)

        radioGroup = findViewById(R.id.radioGroupId)

        val confirmButton = findViewById<Button>(R.id.confirmPreAlertButton)
        confirmButton.setOnClickListener{
            checkButton()
        }

        val spinner = findViewById<Spinner>(R.id.spinnerPreAlert)
        val adapter : ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this,
            R.array.alertOptionsIntervals,
            android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener
        spinner.onItemSelectedListener = this
    }

    private fun checkButton() {
        val radioButtonId = radioGroup.checkedRadioButtonId

        radioButton = findViewById(radioButtonId)

        var option = radioButton.text.toString()
        if (radioButtonId == R.id.radioButtonCustom) {
            val preAlertValueOption = findViewById<EditText>(R.id.editTextNumberPreAlertValue).text.toString()
            option = "$preAlertValueOption $textPreAlertOption"
        }

        val resultIntent = Intent()
        resultIntent.putExtra(PRE_ALARM_OPTION, option)

        setResult(RESULT_OK, resultIntent)

        finish()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        textPreAlertOption = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}