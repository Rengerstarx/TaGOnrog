package com.example.taganroggo.Data

data class Users(val mail: String,
                val name: String,
                val surname: String,
                val score: Int,
                val ava: String,
                val fon: String,
                val visits: MutableList<PlaceData>)
