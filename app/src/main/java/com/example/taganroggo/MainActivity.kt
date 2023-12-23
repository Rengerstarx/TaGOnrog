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
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private var fl = false
    private lateinit var bubble: BubbleTabBar
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private var accelerationValues = FloatArray(3)
    private var lastAccelerationValues = FloatArray(3)
    private var shakeThreshold = 30.5f
    private var minimum_needed_distance = 10.555733555811401E-4
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("187c5f44-6646-457f-b619-eca2dca3cdbe")
        MapKitFactory.initialize(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerPermissionListener()
        checkPermissions()

//https://github.com/akshay2211/BubbleTabBar
        bubble = binding.bubbleTabBar
        replaceFragment(PlaceList())
        bubble.addBubbleListener(object : OnBubbleClickListener {
            override fun onBubbleClick(id: Int) {
                when(id){
                    R.id.List -> {
                        replaceFragment(PlaceList())
                    }
                    R.id.Map -> {
                        replaceFragment(Map())
                    }
                    R.id.Profile -> {
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
                System.arraycopy(accelerationValues, 0, lastAccelerationValues, 0, accelerationValues.size)
                System.arraycopy(event.values, 0, accelerationValues, 0, accelerationValues.size)

                val deltaX = accelerationValues[0] - lastAccelerationValues[0]
                val deltaY = accelerationValues[1] - lastAccelerationValues[1]
                val deltaZ = accelerationValues[2] - lastAccelerationValues[2]

                val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)
                //Log.i("Dibug1", "${acceleration}")
                if (acceleration > shakeThreshold) {
                    isOnPlace()
                    handleShake()
                }
            }
        }
    }
    fun replaceFragment(fragment: Fragment){
        Log.i("Dibug1", "fragment")
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.BAZA,fragment)
        fragmentTransaction.commit()
    }
    private fun handleShake() {
// Код для обработки тряски телефона

    }
    private fun isOnPlace(){
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
        else{
        }
        task.addOnSuccessListener {
            if (it!=null){
                val latitude = it.latitude
                val longitude = it.longitude
                val firebase = FirebaseAPI()
                firebase.takeAll("Places") {
                    val places = ParserPLace().parsPalces(it)
                    var minimum_distance:Double = 100.0
                    var title:String = ""
                    for (item in places) {
                        val distance = calculateDistance(latitude, longitude, item.latitude, item.longitude)
                        if (minimum_distance>distance){
                            minimum_distance = distance
                            title = item.name
                        }
                    }
                    Log.i("info dist", "${title} - ${minimum_distance}")
                    if (minimum_needed_distance >= minimum_distance){
                       replaceFragment(Map())
                    }
                }
            }
        }

        return
    }
    fun checkPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            pLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    private fun registerPermissionListener(){
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){

        }
    }
    fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val deltaX = x2 - x1
        val deltaY = y2 - y1

// Используем теорему Пифагора для вычисления расстояния
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
        return distance
    }
}