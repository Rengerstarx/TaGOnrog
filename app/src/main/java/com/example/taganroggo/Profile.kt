package com.example.taganroggo

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taganroggo.Adapters.PlaceAdapterUser
import com.example.taganroggo.Adapters.RaitAdapter
import com.example.taganroggo.Data.PlaceData
import com.example.taganroggo.Data.Users
import com.example.taganroggo.Parsers.ParceUsers
import com.example.taganroggo.Parsers.ParserPLace
import com.example.taganroggo.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class Profile : Fragment(), PlaceAdapterUser.Listener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: PlaceAdapterUser
    private lateinit var firebase: FirebaseAPI
    private var uid: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initializeComponents()
        fetchUserData()
        setupRatingButton()
        return binding.root
    }

    private fun initializeComponents() {
        adapter = PlaceAdapterUser(requireContext(), this)
        binding.rcPlas.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlas.adapter = adapter
        firebase = FirebaseAPI()
        val sharedPreferences = requireContext().getSharedPreferences("SPDB", Context.MODE_PRIVATE)
        uid = sharedPreferences.getInt("id", 1)
    }

    private fun fetchUserData() {
        firebase.takeOne("Users", uid) { userData ->
            val user = ParceUsers().parsUser(userData)
            loadUserImages(user)
            displayUserDetails(user)
            user.visits.forEach { visit ->
                fetchPlaceData(visit,visit.placeID)
            }
        }
    }

    private fun loadUserImages(user: Users) {
        loadUserImage(user.ava, binding.imageView5)
        loadUserImage(user.fon, binding.imageView)
    }

    private fun loadUserImage(imageUrl: String, imageView: ImageView) {
        firebase.getPicLogo(imageUrl) { picUrl ->
            Picasso.get().load(picUrl).into(imageView)
        }
    }

    private fun displayUserDetails(user: Users) {
        binding.textView5.text = "${user.name} ${user.surname}\nПосещено мест: ${user.visits.count()}"
        binding.textView11.text = user.score.toString()
    }

    private fun fetchPlaceData(placeDataEl: PlaceData, placeID: Int) {
        firebase.takeOne("Places", placeID) { placeData ->
            adapter.createElement(placeDataEl, placeData)
        }
    }

    private fun setupRatingButton() {
        binding.raiting.setOnClickListener {
            showRatingBottomSheet()
        }
    }

    private fun showRatingBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_rait, null)
        dialog.setCancelable(true)
        dialog.setContentView(view)
        populateRatingBottomSheet(view)
        dialog.show()
    }

    private fun populateRatingBottomSheet(view: View) {
        firebase.takeOne("Users", uid) {
            val user = ParceUsers().parsUser(it)
            val image = view.findViewById<ImageView>(R.id.imageView8)
            FirebaseAPI().getPicLogo(user.ava) { it2 ->
                Picasso.get().load(it2).into(image)
            }
            view.findViewById<TextView>(R.id.textView15).text = user.name
            view.findViewById<TextView>(R.id.textView15).text = "Мой рейтинг: ${user.score}"
            val adapter = RaitAdapter()
            view.findViewById<RecyclerView>(R.id.rc).layoutManager =
                LinearLayoutManager(requireContext())
            view.findViewById<RecyclerView>(R.id.rc).adapter = adapter
            FirebaseAPI().takeAll("Users") {
                val users = arrayListOf<Users>()
                for (data in it) {
                    users.add(ParceUsers().parsUser(data))
                }
                val sortedList = users.sortedByDescending { it.score }
                if (sortedList.size >= 5) {
                    for (i in 0..4) {
                        adapter.createElement(sortedList[i])
                    }
                } else {
                    sortedList.forEach { it2 ->
                        adapter.createElement(it2)
                    }
                }
            }
        }
    }

    override fun onClick(placeData: PlaceData, data: DataSnapshot) {
        handlePlaceDataClick(placeData)
    }

    private fun handlePlaceDataClick(placeData: PlaceData) {
        val sharedPreferences = requireContext().getSharedPreferences("SPDB", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getInt("id", 1)
        if (placeData.coment == null || placeData.coment == false) {
            showRatingCard(placeData, uid)
        } else {
            showAlreadyReviewedToast()
        }
    }

    private fun showRatingCard(placeData: PlaceData, uid: Int) {
        binding.cardRate.visibility = View.VISIBLE
        binding.button.setOnClickListener {
            saveUserRating(placeData, uid)
        }
        binding.button2.setOnClickListener {
            hideRatingCard()
        }
    }

    private fun saveUserRating(placeData: PlaceData, uid: Int) {
        val comment = binding.editTextTextPersonName.text.toString()
        val rating = binding.ratingBar.rating.toInt()
        firebase.addComent(placeData.placeID, uid, comment, rating)
        hideRatingCard()
        placeData.coment = true
    }

    private fun hideRatingCard() {
        binding.cardRate.visibility = View.GONE
    }

    private fun showAlreadyReviewedToast() {
        Toast.makeText(requireContext(), "Вы уже оставляли отзыв данному заведению", Toast.LENGTH_LONG).show()
    }
}
