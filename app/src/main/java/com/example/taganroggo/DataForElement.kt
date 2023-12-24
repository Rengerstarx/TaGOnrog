package com.example.taganroggo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.Point

open class DataForElement : ViewModel() {
    val data : MutableLiveData<Place> by lazy {
        MutableLiveData<Place>()
    }
    val flag_view : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val point_user : MutableLiveData<Point> by lazy {
        MutableLiveData<Point>()
    }
}