package com.example.taganroggo

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.ims.ImsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taganroggo.Adapters.PlaceAdapter
import com.example.taganroggo.databinding.FragmentPlaceListBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalTime

class PlaceList : Fragment(), PlaceAdapter.Listener {

    private lateinit var binding: FragmentPlaceListBinding
    val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var nowRadius: Double = 6000.0
    private var allPlaceList: MutableList<Place>? = null
    private var isClosest = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceListBinding.inflate(inflater,container,false)
        val adapter= PlaceAdapter(this, requireContext())
        binding.rcPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlaces.adapter=adapter
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Разрешение еще не предоставлено, запросите его
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
        val firebase = FirebaseAPI()
        firebase.takeAll("Places") {
            val places = ParserPLace().parsPalces(it)
            allPlaceList = places
            adapter.createAll(places)
        }
        binding.root.findViewById<ImageView>(R.id.moreOption).setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
            view.findViewById<SeekBar>(R.id.seekBar4).progress = (nowRadius/1000).toInt() - 1
            view.findViewById<Switch>(R.id.switch1).isChecked = isClosest
            view.findViewById<CardView>(R.id.buttonFilter).setOnClickListener {
                nowRadius = (view.findViewById<SeekBar>(R.id.seekBar4).progress + 1) * 1000.0
                var newList = mutableListOf<Place>()
                if (isLocationEnabled(requireContext()) && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    val locationManager: LocationManager =
                        requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
                    val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val currentLocation = Location("CurrentLocation")
                    currentLocation.latitude = location!!.latitude
                    currentLocation.longitude = location!!.longitude
                    allPlaceList?.forEach {
                        val destinationLocation = Location("DestinationLocation")
                        destinationLocation.latitude = it.latitude
                        destinationLocation.longitude = it.longitude
                        var distanceInMeters = currentLocation.distanceTo(destinationLocation)
                        println(distanceInMeters)
                        if (distanceInMeters <= nowRadius) {
                            newList.add(it)
                        }
                    }
                }
                isClosest = view.findViewById<Switch>(R.id.switch1).isChecked
                if (view.findViewById<Switch>(R.id.switch1).isChecked) {
                    val currentTime = LocalTime.now()
                    val elementsToRemove = mutableListOf<Place>()
                    newList.forEach {
                        val timeRange = it.time
                        val parts = timeRange.split("-").map { it.trim() }
                        val startTime = LocalTime.parse(parts[0])
                        val endTime = LocalTime.parse(parts[1])
                        val isWithinRange = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)
                        if (!isWithinRange) {
                            elementsToRemove.add(it) // Add the element to the removal list
                        }
                    }
                    newList.removeAll(elementsToRemove) // Remove the elements after the iteration

                }
                adapter.createAll(newList)
                dialog.dismiss()
            }
        }
        return binding.root
    }

    override fun onClick(partner: Place) {

    }

    // Обработка результата запроса разрешения
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // Если запрос отменен, массив результатов будет пустым
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    } else {
                    requireActivity().finish()
                }
                return
            }
            // Другие проверки разрешений
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

}