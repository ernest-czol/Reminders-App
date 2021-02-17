package com.example.reminders.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reminders.R
import com.example.reminders.adapter.ReminderAdapter
import com.example.reminders.constants.ConstantsDatabase.COLLECTION_REMINDERS
import com.example.reminders.constants.ConstantsReminder.ID_REMINDER
import com.example.reminders.constants.ConstantsReminder.TITLE_FIELD
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class MainActivity : AppCompatActivity() {
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var collectionReminders: CollectionReference
    private lateinit var reminderAdapter: ReminderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseReference = FirebaseFirestore.getInstance()
        collectionReminders = firebaseReference.collection(COLLECTION_REMINDERS)

        val floatingAddButton = findViewById<FloatingActionButton>(R.id.add_button_reminder)
        floatingAddButton.setOnClickListener(AddButtonClick())

        setUpRecyclerView()
    }

    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            startActivity(Intent(v?.context, NewReminderActivity::class.java))
        }
    }

    private fun setUpRecyclerView() {
        reminderAdapter = buildOptions()

        val recyclerView = buildRecyclerView(reminderAdapter)

        adapterSwypeItem(reminderAdapter, recyclerView)

        adapterSetOnItemClickListener(reminderAdapter)
    }

    override fun onStart() {
        super.onStart()
        reminderAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        reminderAdapter.stopListening()
    }

    private fun buildOptions(): ReminderAdapter {
        val query: Query = collectionReminders.orderBy(TITLE_FIELD, Query.Direction.ASCENDING)

        val options: FirestoreRecyclerOptions<Reminder> =
            FirestoreRecyclerOptions.Builder<Reminder>()
                .setQuery(query, Reminder::class.java)
                .build()

        return ReminderAdapter(options)
    }

    private fun buildRecyclerView(reminderAdapter: ReminderAdapter): RecyclerView {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = reminderAdapter

        return recyclerView
    }

    private fun adapterSetOnItemClickListener(reminderAdapter: ReminderAdapter) {
        reminderAdapter.setOnItemClickListener(object : ReminderAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
//                val reminder = documentSnapshot?.toObject(Reminder::class.java)
//                val path = documentSnapshot?.reference?.path
                val id = documentSnapshot?.id

                val updateIntent = Intent(this@MainActivity, UpdateReminderActivity::class.java)
                updateIntent.putExtra(ID_REMINDER, id)
                startActivity(updateIntent)
            }
        })
    }

    private fun adapterSwypeItem(reminderAdapter: ReminderAdapter, recyclerView: RecyclerView) {
        // Swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val (idAlarm, preAlarms) = reminderAdapter.deleteReminder(viewHolder.adapterPosition)

                val alarmService = AlarmService(applicationContext)
                alarmService.deleteAlarmAndPreAlarms(idAlarm, preAlarms)
            }
        }).attachToRecyclerView(recyclerView)
    }
}