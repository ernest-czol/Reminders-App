package com.example.reminders.util

inline val <reified T> T.TAG: String
    get() = T::class.java.simpleName