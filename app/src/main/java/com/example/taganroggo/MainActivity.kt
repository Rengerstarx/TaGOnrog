package com.example.taganroggo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import com.example.taganroggo.Permission.PermissionHandler
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieDrawable
import com.example.taganroggo.Adapters.ComentAdapter
import com.example.taganroggo.Adapters.PlacePhotoAdapter
import com.example.taganroggo.Data.DataForElement
import com.example.taganroggo.Data.Place
import com.example.taganroggo.Parsers.ParceDate
import com.example.taganroggo.Parsers.ParceUsers
import com.example.taganroggo.Parsers.ParserPLace
import com.example.taganroggo.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.runtime.Error
import io.ak1.BubbleTabBar
import io.ak1.OnBubbleClickListener
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private var is_frag = 1
    private val liveData: DataForElement by viewModels()
    private lateinit var bubble: BubbleTabBar
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var accelerationValues = FloatArray(3)
    private var lastAccelerationValues = FloatArray(3)
    private var shakeThreshold = 30.5f
    private var minimum_needed_distance = 0.010
    private var timer: CountDownTimer? = null
    private var frg: Map? = null
    var dialogView: View? = null
    private lateinit var dialog : Dialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var drivingSession: DrivingSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocation()
        MapKitFactory.setApiKey("187c5f44-6646-457f-b619-eca2dca3cdbe")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getUserLocation()
        StartLocationForMap()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        bubble = binding.bubbleTabBar
        liveData.flag_anim.value = false
        liveData.flag_view.value = false
        replaceFragment(PlaceList())
        setStatusBarColor("#8F847E")
        bubble.addBubbleListener(object : OnBubbleClickListener {
            override fun onBubbleClick(id: Int) {
                when (id) {
                    R.id.List -> {
                        //is_frag = 1
                        replaceFragment(PlaceList())
                    }

                    R.id.Map -> {
                        //frg = Map()
                        replaceFragment(Map())
                        //is_frag = 2
                    }

                    R.id.Profile -> {
                        //is_frag = 3
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

    private fun StartLocationForMap(){
        liveData.cam.value = CameraPosition(Point(47.221183, 38.914698), 13.0f, 0.0f, 0.0f)
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        @RequiresApi(Build.VERSION_CODES.O)
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
                if (acceleration > shakeThreshold - 28.0f){
                    getUserLocation()
                    Log.i("Dibug3", "gdgsgs")
                }
                if (acceleration > shakeThreshold) {
                    isOnPlace()
                }
            }
        }
    }

    fun replaceFragment(fragment: Fragment) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.BAZA, fragment)
            fragmentTransaction.commit()
    }

    fun setStatusBarColor(color: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            val statusBarColor: Int = Color.parseColor(color)
            if (statusBarColor == Color.BLACK && window.getNavigationBarColor() === Color.BLACK) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            window.setStatusBarColor(statusBarColor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    fun isOnPlace() {
                    getUserLocation()
                    val latitude = liveData.point_user.value!!.latitude
                    val longitude = liveData.point_user.value!!.longitude
                    val firebase = FirebaseAPI()
                    firebase.takeAll("Places") {
                        val places = ParserPLace().parsPalces(it)
                        var minimum_distance: Double = 100.0
                        for (item in places) {
                            val distance =
                                calculateDistance(latitude, longitude, item.latitude, item.longitude)
                            if (minimum_distance > distance) {
                                minimum_distance = distance
                                title = item.name
                                liveData.data.value = item
                            }
                        }

                        if (minimum_needed_distance >= minimum_distance && liveData.flag_view.value != true) {
                            liveData.flag_view.value = true
                            val sharedPreferences = getSharedPreferences("SPDB", Context.MODE_PRIVATE)
                            val uid = sharedPreferences.getInt("id", 1)
                            FirebaseAPI().takeLastVisitByUid(uid, liveData.data.value!!.id){
                                var dateLastVisit = it.value.toString().drop(6)
                                val d1 = ParceDate().ToLocalDateFormat(dateLastVisit)
                                var d2 = LocalDate.now()
                                if (d2.isAfter(d1)){
                                    FirebaseAPI().addVisitByUid(uid, liveData.data.value!!.id)
                                    liveData.flag_anim.value = true
                                    ViewInfoForPlace()
                                }
                                else {
                                    liveData.flag_view.value = false
                                    Toast.makeText(baseContext, "Today you have already been to this place.",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }



    @SuppressLint("UseRequireInsteadOfGet")
    fun ViewInfoForPlace(){
        dialogView = layoutInflater.inflate(R.layout.bottom_sheet_for_map, null)
        dialog = BottomSheetDialog(this, R.style.DialogAnimation)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView!!)
        dialog.getWindow()?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        );
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation;
        dialog.getWindow()?.setGravity(Gravity.BOTTOM);


        val btn = dialogView?.findViewById<Button>(R.id.btn_route)
        val image = dialogView?.findViewById<ViewPager2>(R.id.image_list_info)
        val time = dialogView?.findViewById<TextView>(R.id.textTime)
        val addr = dialogView?.findViewById<TextView>(R.id.textAdress)
        val name = dialogView?.findViewById<TextView>(R.id.textName)
        val layoutTags = dialogView?.findViewById<LinearLayout>(R.id.tags_mas)
        val info = dialogView?.findViewById<TextView>(R.id.info_place)

        val adapterPager = PlacePhotoAdapter()
        adapterPager.addImage(liveData.data.value!!.photo)
        image!!.adapter = adapterPager
        time!!.text = liveData.data.value!!.time
        addr!!.text = liveData.data.value!!.adress
        name!!.text = liveData.data.value!!.name
        info!!.text = liveData.data.value!!.info

        var allTags = 0
        layoutTags!!.removeAllViews()
        for (str in liveData.data.value!!.tags) {
            val cardView = CardView(this)
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(5, 5, 5, 5)
            cardView.layoutParams = cardParams
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.mainBlue)) // Устанавливаем цвет фона
            cardView.radius = 40f // Устанавливаем радиус скругления углов

            val textView = TextView(this)
            val textParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = textParams
            textView.text = (" " + str + " ").toString()
            val f = Typeface.create("Roboto", Typeface.NORMAL);
            textView.setTypeface(f)
            textView.textSize = 18f // Устанавливаем размер текста
            textView.setTextColor(ContextCompat.getColor(this, R.color.white)) // Устанавливаем цвет текста
            textView.setPadding(15, 10, 15, 10) // Устанавливаем отступы
            cardView.addView(textView) // Добавляем TextView в CardView
            allTags++

            layoutTags.addView(cardView)
            if (allTags > 3) {
                textView.text = "+${liveData.data.value!!.tags.size - 3}"
                break
            }

            val adapter = ComentAdapter()
            dialogView?.findViewById<RecyclerView>(R.id.rcView)?.layoutManager = LinearLayoutManager(this)
            dialogView?.findViewById<RecyclerView>(R.id.rcView)?.adapter = adapter
            FirebaseAPI().takeOne("Places", liveData.data.value!!.id) {
                it.child("Visitors").children.forEach { it2 ->
                    val key = it2.key.toString().toInt()
                    FirebaseAPI().takeOne("Users", key) { it3 ->
                        val user = ParceUsers().parsUser(it3)
                        adapter.createElement(user, it2.child("Com").value.toString())
                    }
                }
            }

        }
        getUserLocation()
        dialog.show()


        if (liveData.flag_anim.value == true) {
            liveData.flag_anim.value = false
            val anim =
                dialogView?.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.lottiView)
            anim?.visibility = View.VISIBLE
            anim?.setMinProgress(0.0f)
            anim?.setMaxProgress(1.0f)
            anim?.repeatCount = 1
            anim?.repeatMode = LottieDrawable.RESTART
            anim?.playAnimation()
            val mediaPlayer = MediaPlayer.create(this, R.raw.sound)
            mediaPlayer.start()
            val timer = object : CountDownTimer(5000, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                }

                override fun onFinish() {
                    anim?.visibility = View.GONE
                }
            }
            timer.start()
        }

        btn!!.setOnClickListener{
            liveData.flag_view.value = false
            liveData.flag_route.value = true
            dialog.dismiss()
            bubble.setSelected(1)
        }

        dialog.setOnCancelListener {
            liveData.flag_view.value = false
        }
    }



    fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        val deltaX = x2 - x1
        val deltaY = y2 - y1

        // Используем теорему Пифагора для вычисления расстояния
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
        return distance
    }

    fun ListenerForPlace(p : Place){
        liveData.data.value = p
        liveData.flag_view.value = true
        ViewInfoForPlace()
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { it ->
                if (it != null) {
                    liveData.point_user.value = Point(it.latitude, it.longitude)
                }
            }
        return
    }

    fun checkLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 0)
            return
        }
    }

}