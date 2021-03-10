package com.example.reminders.util

import com.firebase.ui.firestore.ObservableSnapshotArray
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.DocumentSnapshot

inline val <reified T> T.TAG: String
    get() = T::class.java.simpleName

/**
 * Empty snapshot array
 */
class EmptySnapshotArray<T> : ObservableSnapshotArray<T>(SnapshotParser<T> {
    TODO()
}) {
    // class EmptyArray<T>(parser: SnapshotParser<T>): ObservableSnapshotArray<T>(parser) {
    override fun getSnapshots(): MutableList<DocumentSnapshot> {
        return mutableListOf()
    }
}