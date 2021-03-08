package com.example.reminders.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reminders.R
import com.example.reminders.adapter.ReminderAdapter
import com.example.reminders.data.Reminder
import com.example.reminders.service.AlarmService
import com.example.reminders.util.EmptySnapshotArray
import com.example.reminders.viewModel.ReminderViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.DocumentSnapshot

class HomeFragment : Fragment() {
    private lateinit var reminderAdapter: ReminderAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var thisContext: Context
    private val viewModel: ReminderViewModel by activityViewModels()
    private val options: FirestoreRecyclerOptions<Reminder> by lazy {
        FirestoreRecyclerOptions.Builder<Reminder>().setSnapshotArray(EmptySnapshotArray()).build()
    }

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

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun setUpRecyclerView() {
        // set up the reminder adapter
        reminderAdapter = ReminderAdapter(options)

        viewModel.getRemindersForAdapter().observe(viewLifecycleOwner, Observer {
            reminderAdapter.updateOptions(it)
        })

        buildRecyclerView(reminderAdapter)

        adapterSwipeItem(reminderAdapter, recyclerView)

        adapterSetOnItemClickListener(reminderAdapter)
    }

    /**
     * Build the recycler view
     */
    private fun buildRecyclerView(reminderAdapter: ReminderAdapter) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(thisContext)
        recyclerView.adapter = reminderAdapter
    }


    /**
     * Navigate to AddReminderFragment
     */
    inner class AddButtonClick : View.OnClickListener {
        override fun onClick(v: View?) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddReminderFragment()
            )
        }
    }

    /**
     * On item clicked navigate to UpdateReminderFragment
     */
    private fun adapterSetOnItemClickListener(reminderAdapter: ReminderAdapter) {
        reminderAdapter.setOnItemClickListener(object : ReminderAdapter.OnItemClickListener {
            override fun onItemClick(documentSnapshot: DocumentSnapshot?, position: Int) {
                val id = documentSnapshot?.id

                id?.let {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToUpdateReminderFragment(id)
                    )
                }
            }
        })
    }

    /**
     * Delete item when swiped
     */
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
                val reminder = reminderAdapter.deleteReminder(viewHolder.adapterPosition)

                // delete the alarms for this reminder
                viewModel.deleteAlarm(AlarmService(thisContext), reminder)
            }
        }).attachToRecyclerView(recyclerView)
    }

    override fun onStart() {
        super.onStart()
        reminderAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        reminderAdapter.stopListening()
    }
}