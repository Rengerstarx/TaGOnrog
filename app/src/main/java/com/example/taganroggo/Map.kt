package com.example.taganroggo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.media.MediaPlayer
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieDrawable
import com.example.taganroggo.Adapters.ComentAdapter
import com.example.taganroggo.Adapters.PlacePhotoAdapter
import com.example.taganroggo.Data.DataForElement
import com.example.taganroggo.Data.Place
import com.example.taganroggo.Parsers.ParceUsers
import com.example.taganroggo.Parsers.ParserPLace
import com.example.taganroggo.databinding.FragmentMapBinding
import com.example.taganroggo.databinding.FragmentProfileBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import kotlin.math.pow
import kotlin.math.sqrt


class Map() : Fragment(), DrivingSession.DrivingRouteListener{
    private var zoomValue: Float = 13.0f
    private lateinit var userPoint : Point
    //val mAct = (activity as MainActivity)
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapKit: MapKit
    private lateinit var dialog : Dialog
    private val liveData: DataForElement by activityViewModels()
    private lateinit var location : UserLocationLayer
    private var placemarkList: ArrayList<PlacemarkMapObject> = arrayListOf()
    var dialogView: View? = null
    var curentLocation: Location? = null
    private lateinit var polyline : PolylineMapObject
    private var count_routing = 0
    lateinit var places : MutableList<Place>
    private var drivingSession:DrivingSession? = null
    private lateinit var bind: FragmentMapBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val mapObjectTapListener = object : MapObjectTapListener {
        override fun onMapObjectTap(mapObject: MapObject, point: Point): Boolean{
            lateinit var obj : Place
            var x = 1.0
            for (i in places){
                val deltaX = i.latitude - point.latitude
                val deltaY = i.longitude - point.longitude
                val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
                if (distance < x){
                    x = distance
                    obj = i
                }
            }
            liveData.data.value = obj
            liveData.flag_view.value = true
            val mAct = (activity as MainActivity)
            mAct.ViewInfoForPlace()
            return true
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentMapBinding.inflate(inflater, container, false)
        InitializeMap()

        bind.centerPos.setOnClickListener {
            ToUserLocation()
        }

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CheckRequestRoute()
    }

    override fun onStop() {
        super.onStop()
        liveData.cam.value = bind.mapview.map.cameraPosition
        bind.mapview.onStop()
        MapKitFactory.getInstance().onStop()
    }

    private fun CheckRequestRoute(){
        if (liveData.flag_route.value == true){
            liveData.flag_route.value = false
            val point = Point(liveData.data.value!!.latitude, liveData.data.value!!.longitude)
            BuildRoute(point)
        }
    }

    private fun InitializeMap(){
        MapKitFactory.initialize(requireContext())
        mapKit = MapKitFactory.getInstance()
        moveToStartLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        MapKitFactory.getInstance().onStart()
        bind.mapview.onStart()

        val firebase = FirebaseAPI()
        firebase.takeAll("Places") {
            places = ParserPLace().parsPalces(it)
            for (i in places) {
                val placemarkMapObject =
                    bind.mapview.map.mapObjects.addPlacemark(
                        Point(i.latitude, i.longitude),
                        ImageProvider.fromResource(requireContext(), R.drawable.icon_for_map)
                    ) // Добавляем метку со значком
                placemarkMapObject.addTapListener(mapObjectTapListener)
                placemarkList.add(placemarkMapObject)
            }
        }

        location = mapKit.createUserLocationLayer(bind.mapview.mapWindow)
        location.isVisible = true
    }
    private fun moveToStartLocation() {
        bind.mapview.map.move(
            liveData.cam.value!!)
    }

    private fun ToUserLocation(){
        bind.mapview.map.move(CameraPosition(liveData.point_user.value!!, 15.0f, 0.0f, 0.0f))
    }



    fun BuildRoute(point: Point){
        liveData.flag_route.value = false
        val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter()
        val drivingOptions = DrivingOptions().apply {
            routesCount = 1
        }
        val vehicleOptions = VehicleOptions()
        var points:ArrayList<RequestPoint> = ArrayList()

        points.add(RequestPoint(liveData.point_user.value!!, RequestPointType.WAYPOINT, null, null))
        points.add(RequestPoint(Point(point.latitude, point.longitude), RequestPointType.WAYPOINT, null, null))
        drivingSession = drivingRouter.requestRoutes(
            points,
            drivingOptions,
            vehicleOptions,
            this@Map
        )
    }
    override fun onDrivingRoutes(p0: MutableList<DrivingRoute>) {
        if (count_routing > 0){
            bind.mapview.map.mapObjects.remove(polyline)
            count_routing = 0
        }
        for(route in p0){
            count_routing++
            polyline = bind.mapview.map.mapObjects.addPolyline(route.geometry)
        }
    }

    override fun onDrivingRoutesError(p0: Error) {
    }
}