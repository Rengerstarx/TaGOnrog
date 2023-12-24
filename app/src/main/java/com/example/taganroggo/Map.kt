package com.example.taganroggo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.location.Location
import com.yandex.mapkit.map.Map
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.telephony.CarrierConfigManager.Gps
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.taganroggo.Adapters.ComentAdapter
import com.example.taganroggo.Adapters.PlaceAdapter
import com.example.taganroggo.Adapters.PlacePhotoAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt


class Map() : Fragment(), DrivingSession.DrivingRouteListener{
    private lateinit var mapView: MapView
    private var zoomValue: Float = 13.0f
    private lateinit var userPoint : Point
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapKit: MapKit
    private lateinit var dialog : Dialog
    private val liveData: DataForElement by activityViewModels()
    private lateinit var location : UserLocationLayer
    private var placemarkList: ArrayList<PlacemarkMapObject> = arrayListOf()
    var dialogView: View? = null
    var curentLocation: Location? = null
    lateinit var places : MutableList<Place>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var drivingSession:DrivingSession? = null
    //lateinit var pinsCollection : MapObjectCollection

    private val mapObjectTapListener = object : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean{
            dialogView = layoutInflater.inflate(R.layout.bottom_sheet_for_map, null)
            dialog = BottomSheetDialog(requireContext(), R.style.DialogAnimation)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(dialogView!!)
            dialog.getWindow()?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation;
            dialog.getWindow()?.setGravity(Gravity.BOTTOM);
            lateinit var obj : Place
            var x = 1.0
            for (i in places){
                val deltaX = i.latitude - point.latitude
                val deltaY = i.longitude - point.longitude
                val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
                if (distance < x){
                    x = distance
                    obj = i
                    Log.i("Dibug1", "${distance}")
                }
            }

            val btn = dialogView?.findViewById<Button>(R.id.btn_route)
            val image = dialogView?.findViewById<ViewPager2>(R.id.image_list_info)
            val time = dialogView?.findViewById<TextView>(R.id.textTime)
            val addr = dialogView?.findViewById<TextView>(R.id.textAdress)
            val name = dialogView?.findViewById<TextView>(R.id.textName)
            val layoutTags = dialogView?.findViewById<LinearLayout>(R.id.tags_mas)
            val info = dialogView?.findViewById<TextView>(R.id.info_place)

            val adapterPager = PlacePhotoAdapter()
            adapterPager.addImage(obj.photo)
            image!!.adapter = adapterPager
            time!!.text = obj.time
            addr!!.text = obj.adress
            name!!.text = obj.name

            var allTags = 0
            layoutTags!!.removeAllViews()
            for (str in obj.tags) {
                val cardView = CardView(requireContext())
                val cardParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                cardParams.setMargins(5, 5, 5, 5)
                cardView.layoutParams = cardParams
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainBlue)) // Устанавливаем цвет фона
                cardView.radius = 40f // Устанавливаем радиус скругления углов

                val textView = TextView(context)
                val textParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textView.layoutParams = textParams
                textView.text = (" " + str + " ").toString()
                val f = Typeface.create("Roboto", Typeface.NORMAL);
                textView.setTypeface(f)
                textView.textSize = 18f // Устанавливаем размер текста
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Устанавливаем цвет текста
                textView.setPadding(15, 10, 15, 10) // Устанавливаем отступы
                cardView.addView(textView) // Добавляем TextView в CardView
                allTags++

                layoutTags.addView(cardView)
                if (allTags > 3) {
                    textView.text = "+${obj.tags.size - 3}"
                    break
                }
            }
            getUserLocation()
            dialog.show()
            dialog.setOnCancelListener {
                liveData.flag_view.value = false
            }

            btn!!.setOnClickListener{
                println("Function click button")
                val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
                val drivingOptions = DrivingOptions().apply {
                    routesCount = 1
                }
                val vehicleOptions = VehicleOptions()
                var points:ArrayList<RequestPoint> = ArrayList()

                points.add(RequestPoint(userPoint, RequestPointType.WAYPOINT, null, null))
                points.add(RequestPoint(Point(point.latitude, point.longitude), RequestPointType.WAYPOINT, null, null))
                drivingSession = drivingRouter.requestRoutes(
                    points,
                    drivingOptions,
                    vehicleOptions,
                    this@Map
                )
                dialog.dismiss()
            }

            return true
        }

        fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            val deltaX = x2 - x1
            val deltaY = y2 - y1

// Используем теорему Пифагора для вычисления расстояния
            val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
            return distance
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MapKitFactory.initialize(requireContext())
        mapKit = MapKitFactory.getInstance()
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = activity?.findViewById(R.id.mapview)!!

        val center_button = activity?.findViewById(R.id.center_pos) as Button

        center_button.setOnClickListener {
            go_to_user_position()
        }

        moveToStartLocation()


        MapKitFactory.getInstance().onStart()
        mapView.onStart()

        val firebase = FirebaseAPI()
        firebase.takeAll("Places") {
            places = ParserPLace().parsPalces(it)
            for (i in places) {
                Log.i("Dibug1", "add ${i.latitude} - ${i.longitude}")
                val placemarkMapObject =
                    mapView.map.mapObjects.addPlacemark(

                        Point(i.latitude, i.longitude),
                        ImageProvider.fromResource(requireContext(), R.drawable.icon_for_map)
                    ) // Добавляем метку со значком
                placemarkMapObject.addTapListener(mapObjectTapListener)
                placemarkList.add(placemarkMapObject)
            }
        }



        location = mapKit.createUserLocationLayer(mapView.mapWindow)
        location.isVisible = true

        checkForView()

    }

    fun checkForView(){
        if (liveData.flag_view.value == true) {
            ViewInfoForPlace()
        }
    }

    private fun moveToStartLocation() {
        mapView.map.move(
            CameraPosition(Point(47.221183, 38.914698), zoomValue, 0.0f, 0.0f))
    }

    private fun go_to_user_position(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        else{
            Log.i("Dibug1", "1222122121212122112")
        }
        task.addOnSuccessListener {
            if(it!=null){
                Log.i("Dibug1", "zaebic")
                val startLoc = Point(it.latitude, it.longitude)
                mapView.map.move(
                    CameraPosition(startLoc, 15.0f, 0.0f, 0.0f))
            }
        }
        //Log.i("tag", "${user_location}")
    }


    @SuppressLint("UseRequireInsteadOfGet")
    fun ViewInfoForPlace(){
        dialogView = layoutInflater.inflate(R.layout.bottom_sheet_for_map, null)
        dialog = BottomSheetDialog(requireContext(), R.style.DialogAnimation)
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
            val cardView = CardView(requireContext())
            val cardParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cardParams.setMargins(5, 5, 5, 5)
            cardView.layoutParams = cardParams
            cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainBlue)) // Устанавливаем цвет фона
            cardView.radius = 40f // Устанавливаем радиус скругления углов

            val textView = TextView(context)
            val textParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = textParams
            textView.text = (" " + str + " ").toString()
            val f = Typeface.create("Roboto", Typeface.NORMAL);
            textView.setTypeface(f)
            textView.textSize = 18f // Устанавливаем размер текста
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white)) // Устанавливаем цвет текста
            textView.setPadding(15, 10, 15, 10) // Устанавливаем отступы
            cardView.addView(textView) // Добавляем TextView в CardView
            allTags++

            layoutTags.addView(cardView)
            if (allTags > 3) {
                textView.text = "+${liveData.data.value!!.tags.size - 3}"
                break
            }

            val adapter = ComentAdapter()
            dialogView?.findViewById<RecyclerView>(R.id.rcView)?.layoutManager = LinearLayoutManager(requireContext())
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
        dialog.setOnCancelListener {
            liveData.flag_view.value = false
        }

        btn!!.setOnClickListener{
            println("Function click button")
            val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
            val drivingOptions = DrivingOptions().apply {
                routesCount = 1
            }
            val vehicleOptions = VehicleOptions()
            var points:ArrayList<RequestPoint> = ArrayList()

            //println(userPoint.latitude)
            points.add(RequestPoint(userPoint, RequestPointType.WAYPOINT, null, null))
            points.add(RequestPoint(Point(liveData.data.value!!.latitude, liveData.data.value!!.longitude), RequestPointType.WAYPOINT, null, null))
            drivingSession = drivingRouter.requestRoutes(
                points,
                drivingOptions,
                vehicleOptions,
                this
            )
            dialog.dismiss()
        }

    }

    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        for(route in p0){
            mapView.map.mapObjects.addPolyline(route.geometry)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
        TODO("Not yet implemented")
    }

    private fun getUserLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
        }
        task.addOnSuccessListener {
            println("gdaojgdakjkvadgddddddddagsgggggggggggggggggggggggggj")
            if (it != null) {
                userPoint = Point(it.latitude, it.longitude)
            }
        }
    }

    //companion object {
    //  fun newInstance() = Map()
    //}

}