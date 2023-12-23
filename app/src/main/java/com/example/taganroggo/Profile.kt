package com.example.taganroggo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.taganroggo.databinding.FragmentProfileBinding
import com.squareup.picasso.Picasso

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        val firebase = FirebaseAPI()
        firebase.takeOne("Users", 1) {
            val user = ParceUsers().parsUser(it)
            FirebaseAPI().getPicLogo(user.ava) { it2 ->
                Picasso.get().load(it2).into(binding.imageView5)
            }
            FirebaseAPI().getPicLogo(user.fon) { it3 ->
                println(user.fon)
                Picasso.get().load(it3).into(binding.imageView)
            }
            binding.textView5.text = "${user.name} ${user.surname}"
        }
        return binding.root
    }

}