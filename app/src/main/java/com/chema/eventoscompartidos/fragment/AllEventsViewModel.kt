package com.chema.eventoscompartidos.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AllEventsViewModel : ViewModel()  {

    private val _text = MutableLiveData<String>().apply {
        value = "This is All Events Fragment"
    }
    val text: LiveData<String> = _text
}