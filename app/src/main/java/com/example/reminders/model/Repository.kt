package com.example.reminders.model

import com.example.reminders.constants.ConstantsDatabase
import com.example.reminders.constants.ConstantsReminder
import com.example.reminders.data.Reminder
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

object Repository {

    /**
     * Get recycler options for the adapter
     * Since firestore automatically updates any changes, there is no need for a flow
     */
    fun getRemindersForAdapter() = flow {
        val collectionReminders =
            FirebaseFirestore.getInstance().collection(ConstantsDatabase.COLLECTION_REMINDERS)

        val query: Query =
            collectionReminders.orderBy(ConstantsReminder.TITLE_FIELD, Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<Reminder>()
            .setQuery(query, Reminder::class.java)
            .build()

        emit(options)
    }

    /**
     * Get a reminder
     * I should improve this implementation
     */
    fun getReminder(idReminder: String) = callbackFlow {
        val docRef = getReminderDocument(idReminder)
        var reminder: Reminder?

        docRef.get().addOnSuccessListener {
            reminder = it.toObject(Reminder::class.java)
            offer(reminder)
        }

        awaitClose()
    }


    /**
     * Add a reminder
     */
    fun addReminder(reminder: Reminder) {
        val collectionReference: CollectionReference =
            FirebaseFirestore.getInstance().collection(ConstantsDatabase.COLLECTION_REMINDERS)

        collectionReference.document(reminder.id).set(reminder)
    }

    /**
     * Get a document of a reminder
     */
    fun getReminderDocument(idReminder: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection(ConstantsDatabase.COLLECTION_REMINDERS)
            .document(idReminder)
    }

    /**
     * Update a reminder
     */
    fun updateReminder(reminder: Reminder) {
        getReminderDocument(reminder.id).set(reminder)
    }

    /**
     * Delete a reminder
     */
    fun deleteReminder(idReminder: String) {
        getReminderDocument(idReminder).delete()
    }

}