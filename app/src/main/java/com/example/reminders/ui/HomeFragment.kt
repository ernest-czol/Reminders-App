package com.example.reminders.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reminders.R
import com.example.reminders.adapter.ReminderAdapter
import com.example.reminders.model.Repository
import com.example.reminders.service.AlarmService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot

class HomeFragment : Fragment() {
    private lateinit var reminderAdapter: ReminderAdapter

    lateinit var recyclerView: RecyclerView
    lateinit var thisContext: Context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val floatingAddButton: FloatingActionButton

        view.apply {
            floatingAddButton = findViewById(R.id.add_button_reminder)
            recyclerView = findViewById(R.id.recycler_view)
        }

        floatingAddButton.setOnClickListener(AddButtonClick())

        setUpRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thisContext = container?.context!!

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
           findNavController().navigate(
               HomeFragmentDirections.actionHomeFragmentToAddReminderFragment()
           )
        }
    }

    private fun setUpRecyclerView() {
        reminderAdapter = buildOptions()

        buildRecyclerView(reminderAdapter)

        adapterSwipeItem(reminderAdapter, recyclerView)

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
        return ReminderAdapter(Repository.getRemindersForAdapter())
    }

    private fun buildRecyclerView(reminderAdapter: ReminderAdapter) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(thisContext)
        recyclerView.adapter = reminderAdapter
    }

    private fun adapterSetOnItemClickListener(reminderAdapter: ReminderAdapter) {
        reminderAdapter.setOnItemClickListener(object : ReminderAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
//                val reminder = documentSnapshot?.toObject(Reminder::class.java)
//                val path = documentSnapshot?.reference?.path
                val id = documentSnapshot?.id

                id?.let{
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToUpdateReminderFragment(id)
                    )
                }
            }
        })
    }

    private fun adapterSwipeItem(reminderAdapter: ReminderAdapter, recyclerView: RecyclerView) {
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

                val alarmService = AlarmService(thisContext)
                alarmService.deleteAlarmAndPreAlarms(idAlarm, preAlarms)
            }
        }).attachToRecyclerView(recyclerView)
    }
}