package com.example.taganroggo

import android.os.Bundle
import android.telephony.ims.ImsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taganroggo.Adapters.PlaceAdapter
import com.example.taganroggo.databinding.FragmentPlaceListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PlaceList : Fragment(), PlaceAdapter.Listener {

    private lateinit var binding: FragmentPlaceListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceListBinding.inflate(inflater,container,false)
        val adapter= PlaceAdapter(this, requireContext())
        binding.rcPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlaces.adapter=adapter
        val firebase = FirebaseAPI()
        firebase.takeAll("Places") {
            val places = ParserPLace().parsPalces(it)
            adapter.createAll(places)
        }
        binding.root.findViewById<ImageView>(R.id.moreOption).setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }
        return binding.root
    }

    override fun onClick(partner: Place) {

    }

}