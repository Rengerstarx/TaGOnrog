package com.example.taganroggo

import android.Manifest
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taganroggo.Adapters.PlaceAdapter.PlaceAdapter
import com.example.taganroggo.Data.Place
import com.example.taganroggo.Parsers.ParserPLace
import com.example.taganroggo.Permission.PermissionHandler
import com.example.taganroggo.databinding.FragmentPlaceListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.time.LocalTime

class PlaceList : Fragment(), PlaceAdapter.Listener {

    private lateinit var binding: FragmentPlaceListBinding
    private lateinit var permissionHandler: PermissionHandler
    private lateinit var adapter: PlaceAdapter
    private var nowRadius: Double = 6000.0
    private var allPlaceList: MutableList<Place>? = null
    private var isClosest = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupBinding(inflater, container)
        setupAdapter()
        checkLocationPermission()
        fetchPlacesFromFirebase()
        setupSearchView()
        setupMoreOptionButton()
        return binding.root
    }

    private fun setupBinding(inflater: LayoutInflater, container: ViewGroup?) {
        binding = FragmentPlaceListBinding.inflate(inflater,container,false)
    }

    private fun setupAdapter() {
        adapter= PlaceAdapter(this, requireContext())
        binding.rcPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlaces.adapter=adapter
    }

    private fun checkLocationPermission() {
        permissionHandler = PermissionHandler(requireContext(), requireActivity())
    }

    private fun fetchPlacesFromFirebase() {
        val firebase = FirebaseAPI()
        firebase.takeAll("Places") {
            val places = ParserPLace().parsPalces(it)
            allPlaceList = places
            adapter.createAll(places)
        }
    }

    private fun setupSearchView() {
        var editor = binding.root.findViewById<androidx.appcompat.widget.AppCompatAutoCompleteTextView>(R.id.searchView)
        editor.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                hideKeyboard(editor)
                search(editor.text.toString())
                return@OnKeyListener true
            }
            false
        })
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupMoreOptionButton() {
        binding.root.findViewById<ImageView>(R.id.moreOption).setOnClickListener {
            showBottomSheetDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
        dialog.setCancelable(true)
        dialog.setContentView(view)
        dialog.show()
        view.findViewById<SeekBar>(R.id.seekBar4).progress = (nowRadius/1000).toInt() - 1
        view.findViewById<Switch>(R.id.switch1).isChecked = isClosest
        view.findViewById<CardView>(R.id.buttonFilter).setOnClickListener {
            nowRadius = (view.findViewById<SeekBar>(R.id.seekBar4).progress + 1) * 1000.0
            val newList = mutableListOf<Place>()
            if (isLocationEnabled(requireContext()) && ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
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
                    val isWithinRange =
                        !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        permissionHandler.handlePermissionResult(requestCode, grantResults)
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun search(str: String) {
        val newList = mutableListOf<Place>()
        allPlaceList?.forEach {
            if (it.name.contains(str, ignoreCase = true)) {
                newList.add(it)
            }
        }
        adapter.createAll(newList)
        isClosest = false
        nowRadius = 6000.0
    }

    override fun onClick(partner: Place) {
        val mAct = (activity as MainActivity)
        mAct.ListenerForPlace(partner)
    }
}
