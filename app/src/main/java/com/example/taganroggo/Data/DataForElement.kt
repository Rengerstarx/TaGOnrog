package com.example.taganroggo.Data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taganroggo.Data.Place
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition

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
    val flag_anim : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val flag_route : MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val cam : MutableLiveData<CameraPosition> by lazy {
        MutableLiveData<CameraPosition>()
    }
}