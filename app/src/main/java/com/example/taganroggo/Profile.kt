package com.example.taganroggo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taganroggo.Adapters.PlaceAdapterUser
import com.example.taganroggo.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: PlaceAdapterUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        adapter = PlaceAdapterUser(requireContext())
        binding.rcPlas.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlas.adapter=adapter
        val firebase = FirebaseAPI()
        firebase.takeOne("Users", 1) {
            val user = ParceUsers().parsUser(it)
            FirebaseAPI().getPicLogo(user.ava) { it2 ->
                Picasso.get().load(it2).into(binding.imageView5)
            }
            FirebaseAPI().getPicLogo(user.fon) { it3 ->
                Picasso.get().load(it3).into(binding.imageView)
            }
            binding.textView5.text = "${user.name} ${user.surname}"
            user.visits.forEach { it4 ->
                FirebaseAPI().takeOne("Places", it4.placeID) { it5 ->
                    adapter.createElement(it4, it5)
                }
            }
        }
        return binding.root
    }

}