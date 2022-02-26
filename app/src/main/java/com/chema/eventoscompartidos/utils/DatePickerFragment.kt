package com.chema.eventoscompartidos.utils

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment (val editText: EditText) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    private var listener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), this, year, month, day)
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val dia = String.format("%02d", day)
        val mes = String.format("%02d", month + 1)
        editText!!.setText("$dia/$mes/$year")
    }
}