package ru.tretyackov.todo

import java.util.Calendar
import java.util.Date

object DateHelper {
    fun dateFromYearMonthDay(year:Int,month:Int,day:Int): Date
    {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(year,month,day)
        return calendar.time
    }
    fun now() = Calendar.getInstance().time
}