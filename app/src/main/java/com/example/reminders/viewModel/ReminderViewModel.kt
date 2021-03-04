package com.example.reminders.viewModel

import androidx.lifecycle.*
import com.example.reminders.constants.ConstantsNotification
import com.example.reminders.data.Reminder
import com.example.reminders.model.Repository
import com.example.reminders.service.AlarmService
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReminderViewModel : ViewModel() {
    private val loadingLiveData: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
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
    fun addReminder(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.addReminder(reminder)
        }
    }

    /**
     * Get a reminder
     */
    fun getReminder(idReminder: String): LiveData<Reminder?> = liveData {
        Repository.getReminder(idReminder).collect {
            emit(it)
        }
    }

    /**
     * Get firestore recycler options for the adapter
     * Since firestore will automatically update any changes, there is no need for live data
     */
    fun getRemindersForAdapter(): FirestoreRecyclerOptions<Reminder> {
        return Repository.getRemindersForAdapter()
    }

    /**
     * Delete the alarms for this reminder
     */
    fun deleteAlarm(alarmService: AlarmService, reminder: Reminder?) {
        alarmService.deleteAlarmAndPreAlarms(reminder?.idAlarm, reminder?.preAlarms)
    }

    fun setExactAlarm(alarmService: AlarmService, reminder: Reminder) {
        alarmService.setExactAlarm(
            reminder.timeInMillis,
            reminder.idAlarm,
            reminder.title,
            reminder.notes
        )
    }

    fun setPreAlarms(alarmService: AlarmService, reminder: Reminder) {
        for (preAlarm in reminder.preAlarms) {
            preAlarm.timeInMillis = reminder.timeInMillis - preAlarm.timeInMillis

            alarmService.setExactAlarm(
                preAlarm.timeInMillis, preAlarm.idPreAlarm,
                ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION, reminder.title
            )
        }
    }

    fun setAlarms(alarmService: AlarmService, reminder: Reminder) {
        setExactAlarm(alarmService, reminder)
        setPreAlarms(alarmService, reminder)
    }

    fun updateAlarms(alarmService: AlarmService, reminder: Reminder) {
        alarmService.updateAlarmAndPreAlarms(
            reminder.idAlarm,
            reminder.timeInMillis,
            reminder.title,
            reminder.notes,
            reminder.preAlarms
        )
    }
}