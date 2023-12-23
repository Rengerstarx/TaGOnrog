package com.example.taganroggo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DataForElement : ViewModel() {
    val data : MutableLiveData<Place> by lazy {
        MutableLiveData<Place>()
    }
    val flag_view : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
}