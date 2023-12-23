package com.example.taganroggo

data class Users(val mail: String,
                val name: String,
                val surname: String,
                val score: Int,
                val ava: String,
                val fon: String,
                val visits: MutableList<PlaceData>)
data class User(val login: String,
                val name: String,
                val photo: MutableList<String>,
                val tags: MutableList<String>,
                val time: String,
                val visitors: MutableList<String>,
                val latitude: Double,
                val longitude: Double)
