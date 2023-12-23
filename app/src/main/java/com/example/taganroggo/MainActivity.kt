package com.example.taganroggo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.taganroggo.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import io.ak1.BubbleTabBar
import io.ak1.OnBubbleClickListener
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private var is_frag = 1
    private val liveData: DataForElement by viewModels()
    private lateinit var bubble: BubbleTabBar
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var accelerationValues = FloatArray(3)
    private var lastAccelerationValues = FloatArray(3)
    private var shakeThreshold = 30.5f
    private var minimum_needed_distance = 10.555733555811401E-4
    private var timer: CountDownTimer? = null
    private var frg: Map? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("187c5f44-6646-457f-b619-eca2dca3cdbe")
        MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

//https://github.com/akshay2211/BubbleTabBar
        bubble = binding.bubbleTabBar
        replaceFragment(PlaceList())
        bubble.addBubbleListener(object : OnBubbleClickListener {
            override fun onBubbleClick(id: Int) {
                when (id) {
                    R.id.List -> {
                        is_frag = 1
                        replaceFragment(PlaceList())
                    }

                    R.id.Map -> {
                        frg = Map()
                        replaceFragment(Map())
                        is_frag = 2
                    }

                    R.id.Profile -> {
                        is_frag = 3
                        replaceFragment(Profile())
                    }
                }
            }
        })


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null) {
            sensorManager.registerListener(
                sensorEventListener,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
// Устройство не поддерживает сенсор ускорения

        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    accelerationValues,
                    0,
                    lastAccelerationValues,
                    0,
                    accelerationValues.size
                )
                System.arraycopy(event.values, 0, accelerationValues, 0, accelerationValues.size)

                val deltaX = accelerationValues[0] - lastAccelerationValues[0]
                val deltaY = accelerationValues[1] - lastAccelerationValues[1]
                val deltaZ = accelerationValues[2] - lastAccelerationValues[2]

                val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
                //Log.i("Dibug1", "${acceleration}")
                if (acceleration > shakeThreshold) {
                    isOnPlace()
                }
            }
        }
    }

    fun replaceFragment(fragment: Fragment) {
        if (is_frag != 2) {
            Log.i("Dibug1", "fragment")
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.BAZA, fragment)
            fragmentTransaction.commit()
        }
    }

    private fun handleShake() {
// Код для обработки тряски телефона

    }

    private fun isOnPlace() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        else {
        }
        task.addOnSuccessListener {
            if (it != null) {
                val latitude = it.latitude
                val longitude = it.longitude
                val firebase = FirebaseAPI()
                firebase.takeAll("Places") {
                    val places = ParserPLace().parsPalces(it)
                    var minimum_distance: Double = 100.0
                    var title: String = ""
                    for (item in places) {
                        val distance =
                            calculateDistance(latitude, longitude, item.latitude, item.longitude)
                        if (minimum_distance > distance) {
                            minimum_distance = distance
                            title = item.name
                            liveData.data.value = item
                        }
                    }
                    Log.i("info dist", "${title} - ${minimum_distance}")
                    Log.i("info dist", "${is_frag}")
                    Log.i("info dist", "${liveData.flag_view.value}")
                    if (minimum_needed_distance >= minimum_distance && liveData.flag_view.value != true && is_frag != 2) {
                        Log.i("info dist", "now fragment - ${is_frag}")
                        Log.i("info dist", "${liveData.data.value!!.time}")
                        liveData.flag_view.value = true
                        if (is_frag != 2) {
                            replaceFragment(Map())
                            is_frag = 2
                        }
                        }
                    }
                }
            }

            return
        }
        fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            val deltaX = x2 - x1
            val deltaY = y2 - y1

// Используем теорему Пифагора для вычисления расстояния
            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
            return distance
        }

}