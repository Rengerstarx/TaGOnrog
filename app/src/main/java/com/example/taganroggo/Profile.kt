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
import com.example.taganroggo.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class Profile : Fragment(), PlaceAdapterUser.Listener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: PlaceAdapterUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        adapter = PlaceAdapterUser(requireContext(), this)
        binding.rcPlas.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlas.adapter=adapter
        val firebase = FirebaseAPI()
        val sharedPreferences = requireContext().getSharedPreferences("SPDB", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getInt("id", 1)
        firebase.takeOne("Users", uid) {
            val user = ParceUsers().parsUser(it)
            FirebaseAPI().getPicLogo(user.ava) { it2 ->
                Picasso.get().load(it2).into(binding.imageView5)
            }
            FirebaseAPI().getPicLogo(user.fon) { it3 ->
                Picasso.get().load(it3).into(binding.imageView)
            }
            binding.textView5.text = "${user.name} ${user.surname}\nПосещено мест: ${user.visits.count()}"
            binding.textView11.text = user.score.toString()
            user.visits.forEach { it4 ->
                FirebaseAPI().takeOne("Places", it4.placeID) { it5 ->
                    adapter.createElement(it4, it5)
                }
            }
        }
        binding.raiting.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val view = layoutInflater.inflate(R.layout.bottom_sheet_rait, null)
            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
            firebase.takeOne("Users", uid) {
                val user = ParceUsers().parsUser(it)
                val image = view.findViewById<ImageView>(R.id.imageView8)
                FirebaseAPI().getPicLogo(user.ava) { it2 ->
                    Picasso.get().load(it2).into(image)
                }
                view.findViewById<TextView>(R.id.textView15).text = user.name
                view.findViewById<TextView>(R.id.textView15).text = "Мой рейтинг: ${user.score}"
                val adapter = RaitAdapter()
                view.findViewById<RecyclerView>(R.id.rc).layoutManager = LinearLayoutManager(requireContext())
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
        return binding.root
    }

    override fun onClick(placeData: PlaceData, data: DataSnapshot) {
        val sharedPreferences = requireContext().getSharedPreferences("SPDB", Context.MODE_PRIVATE)
        val uid = sharedPreferences.getInt("id", 1)
        if (placeData.coment == null || placeData.coment == false) {
            binding.cardRate.visibility = View.VISIBLE
            binding.button.setOnClickListener {
                FirebaseDatabase.getInstance().getReference("Places").child(placeData.placeID.toString()).child("Visitors").child(uid.toString()).child("Com").setValue(binding.editTextTextPersonName.text.toString())
                FirebaseDatabase.getInstance().getReference("Places").child(placeData.placeID.toString()).child("Visitors").child(uid.toString()).child("Rait").setValue(binding.ratingBar.rating.toInt())
                FirebaseDatabase.getInstance().getReference("Users").child(uid.toString()).child("Place").child(placeData.placeID.toString()).child("Coment").setValue(true)
                binding.cardRate.visibility = View.GONE
                placeData.coment = true
            }
            binding.button2.setOnClickListener {
                binding.cardRate.visibility = View.GONE
            }
        } else {
            Toast.makeText(requireContext(), "Вы уже оставляли отзыв данному заведению", Toast.LENGTH_LONG).show()
        }
    }

}