package com.example.reminders.model

import com.example.reminders.constants.ConstantsDatabase
import com.example.reminders.constants.ConstantsReminder
import com.example.reminders.data.Reminder
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object Repository {

    fun getRemindersForAdapter(): FirestoreRecyclerOptions<Reminder> {
        val collectionReminders =
            FirebaseFirestore.getInstance().collection(ConstantsDatabase.COLLECTION_REMINDERS)

        val query: Query =
            collectionReminders.orderBy(ConstantsReminder.TITLE_FIELD, Query.Direction.ASCENDING)

        return FirestoreRecyclerOptions.Builder<Reminder>()
            .setQuery(query, Reminder::class.java)
            .build()
    }

    fun addReminder(reminder: Reminder) {
        val collectionReference: CollectionReference =
            FirebaseFirestore.getInstance().collection(ConstantsDatabase.COLLECTION_REMINDERS)

        collectionReference.add(reminder).addOnSuccessListener {
            reminder.id = it.id
        }
    }

    fun getReminderDocument(idReminder: String): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection(ConstantsDatabase.COLLECTION_REMINDERS)
            .document(idReminder)
    }

    fun updateReminder(reminder: Reminder) {
        getReminderDocument(reminder.id).set(reminder)
    }

}