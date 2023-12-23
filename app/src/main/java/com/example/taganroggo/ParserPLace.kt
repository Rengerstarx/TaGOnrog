package com.example.taganroggo

import android.provider.ContactsContract.Data
import com.google.firebase.database.DataSnapshot


class ParserPLace {

    fun parsPalce(dataSnapshot: DataSnapshot): Place {
        var photos = mutableListOf<String>()
        var tags = mutableListOf<String>()
        var visitors = mutableListOf<String>()
        for (partnerSnapshot in dataSnapshot.child("Photo").children) {
            photos.add(partnerSnapshot.value.toString())
        }
        for (partnerSnapshot in dataSnapshot.child("Tags").children) {
            tags.add(partnerSnapshot.value.toString())
        }
        for (partnerSnapshot in dataSnapshot.child("Visitors").children) {
            visitors.add(partnerSnapshot.value.toString())
        }
        val place = Place(
            adress = dataSnapshot.child("Addres").value.toString(),
            name = dataSnapshot.child("Name").value.toString(),
            photo = photos,
            tags = tags,
            time = dataSnapshot.child("Time").value.toString(),
            visitors = visitors,
            latitude = dataSnapshot.child("Latitude").value.toString().toDouble(),
            longitude = dataSnapshot.child("Longitude").value.toString().toDouble()
        )
        return place
    }

    fun parsPalces(dataSnapshot: MutableList<DataSnapshot>): MutableList<Place> {
        var places = mutableListOf<Place>()
        dataSnapshot.forEach { it ->
            var photos = mutableListOf<String>()
            var tags = mutableListOf<String>()
            var visitors = mutableListOf<String>()
            for (partnerSnapshot in it.child("Photo").children) {
                photos.add(partnerSnapshot.value.toString())
            }
            for (partnerSnapshot in it.child("Tags").children) {
                tags.add(partnerSnapshot.value.toString())
            }
            for (partnerSnapshot in it.child("Visitors").children) {
                visitors.add(partnerSnapshot.value.toString())
            }
            val place = Place(
                adress = it.child("Adress").value.toString(),
                name = it.child("Name").value.toString(),
                photo = photos,
                tags = tags,
                time = it.child("Time").value.toString(),
                visitors = visitors,
                latitude = it.child("Latitude").value.toString().toDouble(),
                longitude = it.child("Longitude").value.toString().toDouble()
            )
            places.add(place)
        }
        return places
    }
}