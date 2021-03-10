package com.example.reminders.data

import com.google.firebase.database.Exclude

/**
 * WeekDays class: an array of booleans representing the week days
 */
data class WeekDays(var days: ArrayList<Boolean> = ArrayList(7)) {
    var currentDayIndex: Int = 0
    var firstDayIndex: Int = 0
        private set
    var lastDayIndex: Int = 0
        private set

    /**
     * Set the current day
     */
    @Exclude
    fun setCurrentDayIndex() {
        currentDayIndex = firstDayIndex
    }

    /**
     * Set the first day
     */
    @Exclude
    fun setFirstDayIndex() {
        for (dayIndex in days.indices)
            if (days[dayIndex]) {
                firstDayIndex = dayIndex
                return
            }
    }

    /**
     * Set the last day
     */
    @Exclude
    fun setLastDayIndex(){
        for (dayIndex in days.indices.reversed())
            if (days[dayIndex]) {
                lastDayIndex = dayIndex
                return
            }
    }

    /**
     * Get the next day
     */
    @Exclude
    fun getNextDayIndex(): Int {
        if (days.isEmpty()) return -1

        for (i in currentDayIndex + 1 until 7)
            if (days[i])
                return i

        return firstDayIndex
    }

    /**
     * Aka an initializer
     */
    @Exclude
    fun setValues(optionWeekDay: BooleanArray) {
        days.clear()
        days.addAll(optionWeekDay.toList())

        setFirstDayIndex()
        setLastDayIndex()
        setCurrentDayIndex()
    }
}