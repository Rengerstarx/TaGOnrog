package com.example.taganroggo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
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
import com.yandex.runtime.image.ImageProvider
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt


class Map() : Fragment() {

    private lateinit var mapView: MapView
    private var zoomValue: Float = 16.5f
    private var startLocation = Point(55.755865, 37.573672)
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapKit: MapKit
    private lateinit var dialog : Dialog
    private var placemarkList: ArrayList<PlacemarkMapObject> = arrayListOf()
    private var name_places : List<String> = listOf(
        "Газлер",
        "Каменка",
        "Чехов сад",
        "Домик Чехова")
    var curentLocation: Location? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    //lateinit var pinsCollection : MapObjectCollection

    private val mapObjectTapListener = object : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean{
            val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_for_map, null)
            dialog = BottomSheetDialog(requireContext(), R.style.DialogAnimation)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(dialogView)
            dialog.getWindow()?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow()?.getAttributes()?.windowAnimations = R.style.DialogAnimation;
            dialog.getWindow()?.setGravity(Gravity.BOTTOM);
            dialog.show()
            return true
        }
    }

    private val mapCameraListener = object : CameraListener{
        override fun onCameraPositionChanged(
            map: Map,
            cameraPosition: CameraPosition,
            cameraUpdateReason: CameraUpdateReason,
            finished: Boolean
        ) {
            if (finished) { // Если камера закончила движение
                println(placemarkList.size)

                for (i in 0..3){
                    println(name_places[i])
                    val point_coord = placemarkList[i].geometry
                    val camera_coord = cameraPosition.target
                    val one = camera_coord.latitude - point_coord.latitude
                    val two = camera_coord.longitude - point_coord.longitude
                    val distance = sqrt(one.pow(2)
                            + two.pow(2))
                    println(distance)
                    if (cameraPosition.zoom > 15.0f){
                        placemarkList[i].setIcon(ImageProvider.fromResource(requireContext(), R.drawable.icon_ex2))
                    }
                    else {
                        placemarkList[i].setIcon(ImageProvider.fromResource(requireContext(), R.drawable.icon_ex))
                    }
                }
                Log.i("Dibug1", "----------------")

                zoomValue = cameraPosition.zoom // После изменения позиции камеры сохраняем величину зума
            }
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


    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = activity?.findViewById(R.id.mapview)!!

        val center_button = activity?.findViewById(R.id.center_pos) as Button

        center_button.setOnClickListener {
            checkPermissions()
            center_user_position()
        }


        MapKitFactory.getInstance().onStart()
        mapView.onStart()
        registerPermissionListener()
        checkPermissions()
        center_user_position()
        //setMarkerInStartLocation()
        //pinsCollection = mapView.map.mapObjects.addCollection()

        val points = listOf(
            Point(47.210041, 38.937439),
            Point(47.213060, 38.938122),
            Point(47.212494, 38.925883),
            Point(47.206874, 38.931242),
        )

        for (i in points) {
            val placemarkMapObject =
                mapView.map.mapObjects.addPlacemark(i, ImageProvider.fromResource(requireContext(), R.drawable.icon_ex)) // Добавляем метку со значком
            placemarkMapObject.addTapListener(mapObjectTapListener)
            placemarkList.add(placemarkMapObject)
        }
        mapView.map.addCameraListener(mapCameraListener)

        var location = mapKit.createUserLocationLayer(mapView.mapWindow)
        location.isVisible = true
    }

    private fun moveToStartLocation(p : Point) {
        mapView.map.move(
            CameraPosition(p, zoomValue, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 5f),
            null)
    }

    private fun center_user_position(){
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
                moveToStartLocation(startLoc)
            }
        }
        //Log.i("tag", "${user_location}")
    }

    fun checkPermissions()  {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
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

    companion object {
        fun newInstance() = Map()
    }

}