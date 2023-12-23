package com.example.taganroggo

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.taganroggo.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import io.ak1.BubbleTabBar
import io.ak1.OnBubbleClickListener

class MainActivity : AppCompatActivity() {
    private lateinit var bubble: BubbleTabBar
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //https://github.com/akshay2211/BubbleTabBar
        bubble = binding.bubbleTabBar
        replaceFragment(PlaceList())
        bubble.addBubbleListener(object : OnBubbleClickListener {
            override fun onBubbleClick(id: Int) {
                when(id){
                    R.id.List -> {
                        replaceFragment(PlaceList())
                    }
                    R.id.Map -> {
                        replaceFragment(Map())
                    }
                    R.id.Profile -> {
                        replaceFragment(Profile())
                    }
                }
            }
        })
    }

    fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.BAZA,fragment)
        fragmentTransaction.commit()
    }
}