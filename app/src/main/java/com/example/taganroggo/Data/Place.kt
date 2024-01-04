package com.example.taganroggo.Data

data class Place(val adress: String,
                 val name: String,
                 val photo: MutableList<String>,
                 val tags: MutableList<String>,
                 val time: String,
                 val visitors: MutableList<String>,
                 val latitude: Double,
                 val longitude: Double,
                 val info: String,
                 val id: Int)