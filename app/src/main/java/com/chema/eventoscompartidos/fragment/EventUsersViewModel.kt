package com.chema.eventoscompartidos.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventUsersViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Events Users Fragment"
    }
    val text: LiveData<String> = _text
}