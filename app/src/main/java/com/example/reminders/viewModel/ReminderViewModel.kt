package com.example.reminders.viewModel

import androidx.lifecycle.*
import com.example.reminders.data.Reminder
import com.example.reminders.model.Repository
import com.example.reminders.service.AlarmService
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ReminderViewModel : ViewModel() {
    private val loadingLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val reminderLiveData: MutableLiveData<Reminder> by lazy {
        MutableLiveData<Reminder>()
    }

    fun getLoading(): LiveData<Boolean> = loadingLiveData

    /**
     * Delete a reminder
     */
    fun deleteReminder(idReminder: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.deleteReminder(idReminder)
        }
    }

    /**
     * Update a reminder
     */
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.updateReminder(reminder)
        }
    }

    /**
     * Add a reminder
     */
    fun addReminder(reminder: Reminder, alarmService: AlarmService) {
        viewModelScope.launch(Dispatchers.IO) {
            // Generate random ID
            reminder.id = UUID.randomUUID().toString()
            // Persist
            Repository.addReminder(reminder)

            // Set the alarms for this reminder
            alarmService.setAlarms(reminder)
        }
    }

    /**
     * Get a reminder
     */
    fun getReminder(idReminder: String): LiveData<Reminder?> {
        viewModelScope.launch {
            Repository.getReminder(idReminder).collect {
                reminderLiveData.postValue(it)
            }
        }

        return reminderLiveData
    }

    /**
     * Get firestore recycler options for the adapter
     * Since firestore will automatically update any changes, there is no need for live data
     */
    fun getRemindersForAdapter(): LiveData<FirestoreRecyclerOptions<Reminder>> = liveData {
        Repository.getRemindersForAdapter().collect {
            emit(it)
        }
    }

    /**
     * Delete the alarms for this reminder
     */
    fun deleteAlarm(alarmService: AlarmService, reminder: Reminder?) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmService.deleteAlarmAndPreAlarms(reminder?.idAlarm, reminder?.preAlarms)
        }
    }

    /**
     * Update alarms for a reminder
     */
    fun updateAlarms(alarmService: AlarmService, reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmService.updateAlarmAndPreAlarms(reminder)
        }
    }
}