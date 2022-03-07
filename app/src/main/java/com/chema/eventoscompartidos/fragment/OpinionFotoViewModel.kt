package com.chema.eventoscompartidos.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OpinionFotoViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Fotos Fragment"
    }
    val text: LiveData<String> = _text
}