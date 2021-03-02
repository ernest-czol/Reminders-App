package com.example.reminders.util

enum class IntervalUnit {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

enum class Months(val days: Int) {
    JANUARY(31),
    FEBRUARY(28),
    MARCH(31),
    APRIL(30),
    MAY(31),
    JUNE(30),
    JULY(31),
    AUGUST(31),
    SEPTEMBER(30),
    OCTOMBER(31),
    NOVEMBER(30),
    DECEMBER(31),
}