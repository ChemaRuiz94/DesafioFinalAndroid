package com.chema.eventoscompartidos.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment (val editText: EditText) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    private var listenerT: TimePickerDialog.OnTimeSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c: Calendar = Calendar.getInstance()
        val hour: Int = c.get(Calendar.HOUR_OF_DAY)
        val minute: Int = c.get(Calendar.MINUTE)

        // Create a new instance of DatePickerDialog and return it
        return TimePickerDialog(requireActivity(), this, hour, minute, DateFormat.is24HourFormat(context))
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val hora = String.format("%02d", hourOfDay)
        VariablesCompartidas.horaEventoActual = hourOfDay
        val minuto = String.format("%02d", minute + 1)
        VariablesCompartidas.minutoEventoActual = minute
        editText!!.setText("$hora:$minuto")
    }

}