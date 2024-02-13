package com.example.taganroggo.Parsers

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class ParceDate {
    fun getDate() : String{
        var date = ""
        val time = Calendar.getInstance().time
        if (time.hours.toString().length == 1){
            date = date + "0" + time.hours.toString()
        }
        else {
            date = date + time.hours.toString()
        }
        date = date + ":"
        if (time.minutes.toString().length == 1){
            date = date + "0" + time.minutes.toString()
        }
        else {
            date = date + time.minutes.toString()
        }
        date = date + " "
        if (time.date.toString().length == 1){
            date = date + "0" + time.date.toString() + "."
        }
        else {
            date = date + time.date.toString() + "."
        }
        if (time.month.toString().length == 1){
            date = date + "0" + (time.month + 1).toString() + "."
        }
        else {
            date = date + time.month.toString() + "."
        }
        date = date + "20" + (time.year - 100).toString()

        return date
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ToLocalDateFormat(date : String) : LocalDate{
        var new_date = ""
        val day = date.dropLast(8)
        var month = date.drop(3)
        month = month.dropLast(5)
        var year = date.drop(6)
        new_date = year + month + day
        var d = LocalDate.parse(new_date, DateTimeFormatter.BASIC_ISO_DATE)
        return d
    }
}