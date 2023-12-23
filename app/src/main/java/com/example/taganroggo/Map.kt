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
import androidx.viewpager2.widget.ViewPager2
import com.example.taganroggo.Adapters.PlacePhotoAdapter
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
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.image.ImageProvider
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt


class Map() : Fragment() {
    private lateinit var mapView: MapView
    private var zoomValue: Float = 13.0f
    private lateinit var pLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var mapKit: MapKit
    private lateinit var dialog : Dialog
    private val liveData: DataForElement by activityViewModels()
    private lateinit var location : UserLocationLayer
    private var placemarkList: ArrayList<PlacemarkMapObject> = arrayListOf()
    var dialogView: View? = null
    var curentLocation: Location? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
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
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainGreen)) // Устанавливаем цвет фона
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
            }

            dialog.show()
            dialog.setOnCancelListener {
                liveData.flag_view.value = false
            }
            return true
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
            cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainGreen)) // Устанавливаем цвет фона
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
        }

        dialog.show()
        dialog.setOnCancelListener {
            liveData.flag_view.value = false
        }

    }

    //companion object {
      //  fun newInstance() = Map()
    //}

}